package com.konovus.apitesting.util

import com.airbnb.epoxy.EpoxyDataBindingLayouts
import com.airbnb.epoxy.PackageModelViewConfig
import com.konovus.apitesting.R

@PackageModelViewConfig(rClass = R::class)
@EpoxyDataBindingLayouts(value = [R.layout.transactions_item])
interface EpoxyConfig