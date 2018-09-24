package com.c51.sedwards.c51challenge.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.c51.sedwards.c51challenge.R;
import com.c51.sedwards.c51challenge.model.Offer;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
    public static final RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions()
            .error(R.drawable.icons8_wallpaper_96)
            .placeholder(R.drawable.icons8_image_file_96)
            .fallback(R.drawable.icons8_wallpaper_96)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

    private List<Offer> mOffers;

    public OfferAdapter(List<Offer> offers) {
        mOffers = offers;
    }

    public void setData(final List<Offer> offers) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mOffers != null ? mOffers.size() : 0;
            }

            @Override
            public int getNewListSize() {
                return offers != null ? offers.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mOffers.get(oldItemPosition).getOfferId().equals(offers.get(newItemPosition).getOfferId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                final Offer oldOffer = mOffers.get(oldItemPosition);
                final Offer newOffer = offers.get(newItemPosition);
                return oldOffer.getImageUrl()
                        .equals(newOffer.getImageUrl()) &&
                        oldOffer.getCashBack() == newOffer.getCashBack() &&
                        oldOffer.getName().equals(newOffer.getName());
            }
        });
        mOffers = offers;
        result.dispatchUpdatesTo(this);
    }
    /**
     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * . Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     */
    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item, parent, false);
        return new OfferViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the  to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use  which will
     * have the updated adapter position.
     * <p>
     * Override  instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        if (null == mOffers || mOffers.size() < position) {
            return;
        }
        final Offer offer = mOffers.get(position);
        holder.mTitleText.setText(offer.getName());
        holder.mCashBackText.setText(holder.mCashBackText.getResources()
                .getString(R.string.cash_back, offer.getCashBack()));
        Glide.with(holder.mIconImage)
                .applyDefaultRequestOptions(DEFAULT_REQUEST_OPTIONS)
                .load(offer.getImageUrl())
                .into(holder.mIconImage);

        if (holder.itemView.hasOnClickListeners()) {
            holder.itemView.setOnClickListener(null);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add to selected offers
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return null != mOffers ? mOffers.size() : 0;
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleText;
        ImageView mIconImage;
        TextView mCashBackText;

        OfferViewHolder(View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.offer_title);
            mIconImage = itemView.findViewById(R.id.offer_image);
            mCashBackText = itemView.findViewById(R.id.offer_cash_back);
        }
    }

}
