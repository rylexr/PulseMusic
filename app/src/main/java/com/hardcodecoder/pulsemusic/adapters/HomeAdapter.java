package com.hardcodecoder.pulsemusic.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textview.MaterialTextView;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.helper.MediaArtHelper;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private List<MusicModel> mList;
    private ItemClickListener.Simple mListener;
    @LayoutRes
    private int mLayout;
    private LayoutStyle mLayoutStyle;
    private LayoutInflater mInflater;

    public HomeAdapter(LayoutInflater inflater, List<MusicModel> list, ItemClickListener.Simple clickListener, LayoutStyle style) {
        this.mListener = clickListener;
        this.mList = list;
        this.mLayoutStyle = style;
        this.mInflater = inflater;
        if (style == LayoutStyle.CIRCLE) mLayout = R.layout.rv_home_item_cir;
        else mLayout = R.layout.rv_home_item_sq;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(mLayout, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItemData(mList.get(position), mLayoutStyle);
    }

    @Override
    public int getItemCount() {
        if (mList != null)
            return mList.size();
        return 0;
    }

    public enum LayoutStyle {
        CIRCLE,
        ROUNDED_RECTANGLE
    }

    /*
     * Custom View holder class
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView title, text;
        private ImageView albumArt;

        MyViewHolder(View itemView, ItemClickListener.Simple listener) {
            super(itemView);

            title = itemView.findViewById(R.id.home_rv_list_item_title);
            text = itemView.findViewById(R.id.home_rv_list_item_text);
            albumArt = itemView.findViewById(R.id.home_rv_list_item_album_art);

            itemView.setOnLongClickListener(v -> {
                listener.onOptionsClick(v, getAdapterPosition());
                return true;
            });
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
        }

        void setItemData(MusicModel md, LayoutStyle style) {
            title.setText(md.getTrackName());
            text.setText(md.getArtist());

            GlideApp.with(albumArt)
                    .load(md.getAlbumArtUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            MediaArtHelper.getMediaArtDrawableAsync(
                                    itemView.getContext(),
                                    new int[]{albumArt.getWidth(), albumArt.getHeight()}, md.getAlbumId(),
                                    style == LayoutStyle.ROUNDED_RECTANGLE ?
                                            MediaArtHelper.RoundingRadius.RADIUS_8dp :
                                            MediaArtHelper.RoundingRadius.CIRCLE,
                                    drawable -> albumArt.setImageDrawable(drawable));
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .transform(new MultiTransformation<>(GlideConstantArtifacts.getCenterCrop(),
                            style == LayoutStyle.ROUNDED_RECTANGLE ?
                                    GlideConstantArtifacts.getRadius8dp() :
                                    GlideConstantArtifacts.getCircleCrop()))
                    .into(albumArt);
        }
    }
}
