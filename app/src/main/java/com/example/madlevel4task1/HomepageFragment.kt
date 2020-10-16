package com.example.madlevel4task1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
        getShoppingListFromDatabase()

        initRv()

        fabAddProduct.setOnClickListener{
            showAddProductDialogue()
        }

        fabDeleteAll.setOnClickListener {
            removeAllProducts()
        }
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

    @SuppressLint("InflateParams")
    private fun showAddProductDialogue() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_product_dialog_title))
        val dialogLayout = layoutInflater.inflate(R.layout.add_product_popup, null)
        val productName = dialogLayout.findViewById<EditText>(R.id.txt_product_name)
        val amount = dialogLayout.findViewById<EditText>(R.id.txt_amount)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.dialog_ok_btn) { _: DialogInterface, _: Int ->
            addProduct(productName, amount)
        }
        builder.show()
    }

    private fun addProduct(txtProductName: EditText, txtAmount: EditText) {
        if (validateFields(txtProductName, txtAmount)) {
            mainScope.launch {
                val product = Product(
                        productName = txtProductName.text.toString(),
                        productQuantity = txtAmount.text.toString().toShort()
                )

                withContext(Dispatchers.IO) {
                    productRepository.insertProduct(product)
                }

                getShoppingListFromDatabase()
            }
        }
    }

    private fun validateFields(txtProductName: EditText, txtAmount: EditText): Boolean {
        return if (txtProductName.text.toString().isNotBlank()
                && txtAmount.text.toString().isNotBlank()
        ) {
            true
        } else {
            Toast.makeText(activity, "Please fill in the fields", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun removeAllProducts() {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                productRepository.deleteAllProducts()
            }
            getShoppingListFromDatabase()
        }
    }
}