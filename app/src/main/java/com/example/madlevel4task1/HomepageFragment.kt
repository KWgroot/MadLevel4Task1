package com.example.madlevel4task1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomepageFragment : Fragment() {

    private lateinit var productRepository: ProductRepository
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val shoppingList = arrayListOf<Product>()
    private val shoppingListAdapter = ShoppingListAdapter(shoppingList)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productRepository = ProductRepository(requireContext())

        initRv()
    }

    private fun initRv() {
        /*viewManager = LinearLayoutManager(activity) //????? where does that viewManager come from?
        rvShoppingList.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        createItemTouchHelper().attachToRecyclerView(rvShoppingList)

        rvShoppingList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = shoppingListAdapter
        }*/ // None of this makes any sense knowing the example. As such I will use the example's method here.

        rvShoppingList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvShoppingList.adapter = shoppingListAdapter
        rvShoppingList.setHasFixedSize(true)
        rvShoppingList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        createItemTouchHelper().attachToRecyclerView(rvShoppingList)
    }

    private fun getShoppingListFromDatabase() {
        mainScope.launch {
            val shoppingList = withContext(Dispatchers.IO) {
                productRepository.getAllProducts()
            }
            this@HomepageFragment.shoppingList.clear()
            this@HomepageFragment.shoppingList.addAll(shoppingList)
            this@HomepageFragment.shoppingListAdapter.notifyDataSetChanged()
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val productToDelete = shoppingList[position]
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        productRepository.deleteProduct(productToDelete)
                    }
                    getShoppingListFromDatabase()
                }
            }
        }
        return ItemTouchHelper(callback)
    }
}