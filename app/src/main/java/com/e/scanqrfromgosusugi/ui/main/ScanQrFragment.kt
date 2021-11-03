package com.e.scanqrfromgosusugi.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.e.scanqrfromgosusugi.databinding.ScanQrFragmentBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class ScanQrFragment : Fragment() {
    val TAG = "scan_result"
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: ScanQrFragmentBinding
    lateinit var codeScanner: CodeScanner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ScanQrFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScanner()
        viewModel.scanQrResult.observe(requireActivity()) {
            Log.d(TAG, it)
            val params = it.split('_')

            // Если результат сканирования содержит ошибку или положительный результат сертификата о вакцинации ( только 1 параметр )
            if (params.size == 1) {
                /*AlertDialog.Builder(requireContext()).apply {
                    setTitle("Результат сканирования QR Госуслуг:")
                    setMessage(it)
                    setPositiveButton("Ок") { dialog, _ ->
                        dialog.cancel()
                        codeScanner.startPreview()
                    }
                } .create().show()*/
                when {
                    params[0] == "expired" -> {
                        CustomDialogFragment(false, null).show(parentFragmentManager, "customDialog")
                    }
                    params[0] == "1" -> {
                        CustomDialogFragment(true, null).show(parentFragmentManager, "customDialog")
                    }
                    params[0] == "error" -> {
                        CustomDialogFragment(false, null).show(parentFragmentManager, "customDialog")
                    }
                    params[0] == "load_error" -> {
                        CustomDialogFragment(null, "Ошибка при загрузке данных").show(parentFragmentManager, "customDialog")
                    }
                }
            }
            // Если результат сканирования вернул подробный результат
            if (params.size == 3) {
                /*AlertDialog.Builder(requireContext()).apply {
                    if (params[0] == "3" || params[2] == "1") {
                        //setMessage("Срок истёк ${ params[2] }")
                        setMessage("QR код не действителен")
                    }
                    if (params[0] == "1") {
                        setMessage("QR код действителен до ${params[2]}")
                    }

                    setTitle(params[1])
                    setPositiveButton("Ок"){ dialog, _ ->
                        dialog.cancel()
                        codeScanner.startPreview()
                    }
                } .create().show()*/

                if (params[0] == "3" || params[2] == "1") {
                    CustomDialogFragment(false, null).show(parentFragmentManager, "customDialog")
                }
                if (params[0] == "1") {
                    CustomDialogFragment(true, null).show(parentFragmentManager, "customDialog")
                }
            }
        }
        viewModel.refresh.observe(requireActivity()) {
            if (it) {
                codeScanner.startPreview()
                viewModel.refresh.value = false
            }
        }
    }

    // Функция настройки сканера
    private fun setScanner() {
        // Установка параметров сканера
        codeScanner = CodeScanner(requireContext(), binding.scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
            startPreview()
        }

        // Обработка "тапа" по сканеру ( размораживает его )
        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        codeScanner.decodeCallback = DecodeCallback {
            parseGosUslugi(it.text)
        }

        // Callback для ошибок в работе сканера
        codeScanner.errorCallback = ErrorCallback {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    //  Функция обращается к Api Госуслуг и получает информацию о COVID-тесте или истории болезни. Результат помещается в свойство viewModel.scanQrResult
    private fun parseGosUslugi(url: String) {
        GlobalScope.launch {
            // Api госуслуги для анализа на COVID-19: https://www.gosuslugi.ru/api/covid-cert/v3/cert/check/7000000015474309?lang=ru&ck=784c717b43c71fd66f292b6e1390ba04 | Пример ссылки для анализа: https://www.gosuslugi.ru/covid-cert/verify/7000000015474309?lang=ru&ck=784c717b43c71fd66f292b6e1390ba04
            // Api госуслуги сертификата о вакцинации: https://www.gosuslugi.ru/api/vaccine/v1/cert/verify/a57a91c6-be7c-4661-a1a3-daae23ee950d || Пример https://www.gosuslugi.ru/vaccine/cert/verify/a57a91c6-be7c-4661-a1a3-daae23ee950d
            val originUrl = url.toString()
            val testUrl = url.toString()
            try {
                when {
                    originUrl.contains("https://www.gosuslugi.ru/covid-cert/verify/") -> {
                        try {
                            val newUrl = testUrl.replace("https://www.gosuslugi.ru/covid-cert/verify/", "https://www.gosuslugi.ru/api/covid-cert/v3/cert/check/")
                            val response = URL(newUrl).readText()
                            val status = JSONObject(JSONArray(JSONObject(response).getString("items"))[0].toString()).getString("status")
                            val expiredAt = JSONObject(JSONArray(JSONObject(response).getString("items"))[0].toString()).getString("expiredAt")
                            val expired = JSONObject(JSONArray(JSONObject(response).getString("items"))[0].toString())
                            // Если у QR-кода вакцинации истёк срок действия
                            if (expired.has("expired")) {
                                //viewModel.scanQrResult.postValue("Срок действия истёк.")
                                viewModel.scanQrResult.postValue("expired")
                            }
                            // Если он действительный, то вернём подробную информацию
                            else {
                                viewModel.scanQrResult.postValue(status  + '_' + "Анализ на COVID-19" + '_' + expiredAt)
                            }
                        }
                        catch (ex: Exception) {
                            //viewModel.scanQrResult.postValue( "Ошибка: covid-cert")
                            viewModel.scanQrResult.postValue( "error")
                            Log.d(TAG, "covid-cert: $ex")
                        }
                    }
                    originUrl.contains("https://www.gosuslugi.ru/vaccine/cert/") -> {
                        try {
                            val newUrl = testUrl.replace("https://www.gosuslugi.ru/vaccine/cert/verify/", "https://www.gosuslugi.ru/api/vaccine/v1/cert/verify/")
                            val response = URL(newUrl).readText()
                            val status = JSONObject(response).getString("status")
                            // Сертификат со статусом 1 - действительный
                            if (status == "1") {
                                //viewModel.scanQrResult.postValue( "Сертификат действителен")
                                viewModel.scanQrResult.postValue( "1")
                            }
                            // В любом другом случае будем считать его не действительным
                            else {
                                //viewModel.scanQrResult.postValue( "Сертификат не действителен" + '_' + "Сертификат о вакцинации " + '_' + "")
                                viewModel.scanQrResult.postValue( "error")
                            }
                        }
                        catch (ex: Exception) {
                            //viewModel.scanQrResult.postValue( "Ошибка: некорректный QR код")
                            viewModel.scanQrResult.postValue( "error")
                            Log.d(TAG, "vaccine: $ex")
                        }
                    }
                    else -> {
                        // viewModel.scanQrResult.postValue("Не корректный QR")
                        viewModel.scanQrResult.postValue("error")
                    }
                }
            } catch (ex: Exception) {
                //viewModel.scanQrResult.postValue("Ошибка при получени данных")
                viewModel.scanQrResult.postValue("load_error")
                Log.d(TAG, "parseGosUslugi: $ex")
            }
        }
    }
}