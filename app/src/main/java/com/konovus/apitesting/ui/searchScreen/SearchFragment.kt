package com.konovus.apitesting.ui.searchScreen

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import com.konovus.apitesting.R
import com.konovus.apitesting.data.local.entities.CompanyInfo
import com.konovus.apitesting.databinding.SearchFragmentBinding
import com.konovus.apitesting.util.Constants.TAG
import com.konovus.apitesting.util.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.search_fragment), SearchAdapter.OnItemClickListener {

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SearchFragmentBinding.bind(view)
        viewModel.updateBottomNavSelectedId(R.id.searchFragment)

        setupSearchView()
        bindLayoutData()
    }

    private fun bindLayoutData() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            Log.i(TAG, "SearchFragment: bindLayoutData: $state")
            binding.progressBar.isVisible = state.isLoading
            binding.swipeRefreshLayout.isRefreshing = state.isRefreshing

            state.companies.asLiveData().distinctUntilChanged().observe(viewLifecycleOwner) {
                val adapter = SearchAdapter(this@SearchFragment)
                binding.recyclerView.adapter = adapter
                adapter.submitData(lifecycle, it)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchView() {
        binding.apply {
            searchView.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isNotEmpty())
                    viewModel.onEvent(SearchScreenEvent.OnSearchQueryChange(text.toString()))
            }

            searchView.setOnFocusChangeListener { _, isFocused ->
                if (isFocused) {
                    searchView.hint = null
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_back_arrow, 0, R.drawable.ic_close, 0)
                    val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
                } else {
                    searchView.hint = "Search..."
                    searchView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_search_24,0)
                    val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                }
            }

            searchView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (searchView.right - (searchView.compoundDrawables[2].bounds.width() + 60))) {
                        if (!searchView.isFocused) {
                            searchView.requestFocus()
                            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
                        } else
//                            if (searchView.text.isNotEmpty())
                            {
                            searchView.setText("")
                            viewModel.onEvent(SearchScreenEvent.OnSearchQueryChange(""))
                        }
                        return@setOnTouchListener true
                    }
                    if (searchView.compoundDrawables[0] != null &&
                        event.rawX <= (searchView.left + searchView.compoundDrawables[0].bounds.width() + 60)) {
                            searchView.setText("")
                            searchView.clearFocus()
                            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(searchView.windowToken, InputMethodManager.SHOW_FORCED)
                            return@setOnTouchListener true
                        }
                }
                false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (binding.searchView.text.isNotEmpty())
            binding.searchView.requestFocus()
    }

    override fun onItemClick(company: CompanyInfo, position: Int) {
        val action =
            SearchFragmentDirections.actionSearchFragmentToInfoFragment(company.name, company.symbol)
        findNavController().navigate(action)
    }
}