package de.handler.mobile.smartdoorbell

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private var doorbellItemAdapter: DoorbellItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this.applicationContext)
        databaseReference = FirebaseDatabase.getInstance().reference.child("logs")
        recyclerView = findViewById(R.id.activity_main_recycler_view)

        doorbellItemAdapter = DoorbellItemAdapter(databaseReference)
        doorbellItemAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                recyclerView.smoothScrollToPosition(doorbellItemAdapter!!.itemCount)
            }
        })

        recyclerView.adapter = doorbellItemAdapter
    }

    override fun onStop() {
        super.onStop()
        doorbellItemAdapter?.cleanup()
    }
}
