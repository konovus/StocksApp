package com.konovus.apitesting.data.validator

import android.content.Context
import android.content.res.Resources
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.OrderType
import com.konovus.apitesting.data.local.entities.Portfolio
import com.konovus.apitesting.data.local.entities.Stock
import com.konovus.apitesting.util.toNDecimals
import javax.inject.Inject

class TransactionValidator @Inject constructor(
    private val input: String,
    private val orderType: OrderType,
    private val portfolio: Portfolio,
    private val stock: Stock,
    context: Context
): IValidator {

    val res: Resources = context.resources

    override fun validate(): ValidateResult {
        if (input.isEmpty()) return ValidateResult(isSuccess = true)

        if (input.toDoubleOrNull() == null)
            return ValidateResult(isSuccess = false, message = res.getString(R.string.valid_number))

        if (input.toDouble() !in 1.0..10000.0)
            return ValidateResult(isSuccess = false, message = res.getString(R.string.amount_between))

        if (orderType == OrderType.Sell) {
            if (portfolio.stocksToShareAmount.isEmpty())
                return ValidateResult(isSuccess = false, message = res.getString(R.string.no_shares_to_sell))

            val amountToSell = input.toDouble()
            val amountOwned = portfolio.stocksToShareAmount.getValue(stock.symbol)
                .times(stock.price).toNDecimals(2)
            return if (amountToSell > amountOwned)
                ValidateResult(
                    isSuccess = false,
                    message = res.getString(R.string.max_to_sell, amountOwned.toString())
                )
            else ValidateResult(isSuccess = true)
        } else return ValidateResult(isSuccess = true)
    }
}