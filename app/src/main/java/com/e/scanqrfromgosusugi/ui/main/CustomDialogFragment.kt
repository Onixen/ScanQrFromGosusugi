package com.e.scanqrfromgosusugi.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.e.scanqrfromgosusugi.R
import com.e.scanqrfromgosusugi.databinding.FragmentCustomDialogBinding

class CustomDialogFragment(val valid: Boolean?, val text: String?) : DialogFragment() {
    private lateinit var binding: FragmentCustomDialogBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCustomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (valid != null) {
            if (valid) {
                binding.dialogContainer.setBackgroundResource(R.drawable.custom_dialog_bg_valid)
                binding.textDialog.text = "QR код действителен"
            }
            else {
                binding.dialogContainer.setBackgroundResource(R.drawable.custom_dialog_bg_invalid)
                binding.textDialog.text ="QR код не действителен"
            }
        } else {
            binding.dialogContainer.setBackgroundColor(resources.getColor(R.color.other_errors))
            binding.textDialog.text = text?: "Непредвиденная ошибка"
        }

        binding.dismissBtn.setOnClickListener {
            dismiss()
            viewModel.refresh.value = true
        }
    }

    override fun onDestroy() {
        viewModel.refresh.value = true
        super.onDestroy()
    }
}