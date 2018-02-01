package de.handler.mobile.smartdoorbell

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference


class DoorbellItemAdapter(ref: DatabaseReference) :
        FirebaseRecyclerAdapter<DoorbellItem, DoorbellItemAdapter.ViewHolder>(
                DoorbellItem::class.java,
                R.layout.item_doorbell,
                ViewHolder::class.java,
                ref) {

    override fun populateViewHolder(viewHolder: ViewHolder?, item: DoorbellItem, position: Int) {
        viewHolder?.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView = itemView.findViewById(R.id.item_image)
        var timeTextView: TextView = itemView.findViewById(R.id.item_time_text_view)
        var metaDataTextView: TextView = itemView.findViewById(R.id.item_metadata_text_view)

        fun bind(item: DoorbellItem) {
            // Set bitmap
            var image: Bitmap? = null
            if (!TextUtils.isEmpty(item.image)) {
                val imageByteArray = Base64.decode(item.image, Base64.NO_WRAP or Base64.URL_SAFE)
                image = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            }
            itemImage.setImageBitmap(image)

            // Set time
            timeTextView.text = DateUtils.getRelativeDateTimeString(
                    itemView.context,
                    item.timestamp!!,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0)

            metaDataTextView.text = item.annotations?.keys?.joinToString { it }
        }
    }
}