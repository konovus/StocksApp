package com.konovus.apitesting.data.validator

import android.content.Context
import com.konovus.apitesting.R

class NullOrEmptyValidator(
    private val input: String?,
    context: Context
): IValidator {

    private val res = context.resources

    override fun validate(): ValidateResult {
        return if (input.isNullOrEmpty())
            ValidateResult(isSuccess = false, message = res.getString(R.string.empty_input))
        else ValidateResult(isSuccess = true)
    }
}