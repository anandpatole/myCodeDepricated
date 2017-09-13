package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowFooterProgressBinding;
import com.cheep.databinding.RowTabChatBinding;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pankaj on 9/29/16.
 */

public class ChatTabRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_PROGRESS = 0;
    private final int VIEW_TYPE_MESSAGE = 1;

    private static final String TAG = ChatTabRecyclerViewAdapter.class.getSimpleName();

    ArrayList<TaskChatModel> mList = new ArrayList<>();
    ChatItemInteractionListener listener;
    private Context mContext;

    public ChatTabRecyclerViewAdapter(Context mContext, ChatItemInteractionListener listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    public void appendMessageList(List<TaskChatModel> messages) {
        this.mList.addAll(messages);
        notifyDataSetChanged();
    }

    public void updateMessage(TaskChatModel message) {
        for (int i = 0; i < this.mList.size(); i++) {
            if (this.mList.get(i).chatId.equalsIgnoreCase(message.chatId)) {
                this.mList.set(i, message);
                performShorting();
                notifyDataSetChanged();
                return;
            }
        }
        this.mList.add(message);
        performShorting();
        notifyDataSetChanged();
    }

    private void performShorting() {
        Collections.sort(this.mList, new Comparator<TaskChatModel>() {
            @Override
            public int compare(TaskChatModel t1, TaskChatModel t2) {
                return String.valueOf(t1.timestamp).compareTo(String.valueOf(t2.timestamp));
            }
        });
        Collections.reverse(this.mList);
    }

    public void deleteMessage(TaskChatModel message) {
        for (int i = 0; i < this.mList.size(); i++) {
            if (this.mList.get(i).chatId.equalsIgnoreCase(message.chatId)) {
                this.mList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Used to get first message timestamp from messages
     *
     * @return
     */
    public Long getFirstTimestamp() {
        if (mList.size() > 0)
            return mList.get(0).timestamp;
        else
            return 0l;
    }

    /**
     * Used to get last message timestamp from messages
     *
     * @return
     */
    public Long getLastTimestamp() {
        if (mList.size() > 0)
            return mList.get(mList.size() - 1).timestamp;
        else
            return 0l;
    }

    /**
     * used to show progressbar
     */
    public void showProgressBar() {
        this.mList.add(this.mList.size(), null);
        notifyItemInserted(this.mList.size());
    }

    /**
     * Used to hide progressbar
     */
    public void hideProgressBar() {
        if (this.mList.size() > 0) {
            TaskChatModel taskChatModel = this.mList.get(this.mList.size() - 1);
            if (taskChatModel == null) {
                this.mList.remove(this.mList.size() - 1);
                notifyItemRemoved(this.mList.size() - 1);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        TaskChatModel taskChatModel = mList.get(position);
        if (taskChatModel == null) {
            return VIEW_TYPE_PROGRESS;
        } else {
            return VIEW_TYPE_MESSAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_PROGRESS:
                RowFooterProgressBinding rowFooterProgressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_footer_progress, parent, false);
                viewHolder = new ChatTabRecyclerViewAdapter.ViewProgressHolder(rowFooterProgressBinding);
                break;
            case VIEW_TYPE_MESSAGE:
                RowTabChatBinding mRowTabChatBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_tab_chat, parent, false);
                viewHolder = new ChatTabRecyclerViewAdapter.ViewHolder(mRowTabChatBinding);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final TaskChatModel model = mList.get(viewHolder.getAdapterPosition());
        if (viewHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            if (model.unreadCount > 0) {
                holder.mRowTabHomeBinding.imgUnread.setVisibility(View.VISIBLE);
            } else {
                holder.mRowTabHomeBinding.imgUnread.setVisibility(View.INVISIBLE);
            }

            // Need to Change last sent message based on type
            switch (model.messageType) {
                case Utility.CHAT_TYPE_MONEY:
                    holder.mRowTabHomeBinding.textDesc.setText(mContext.getString(R.string.label_quote_received));
                    break;
                case Utility.CHAT_TYPE_MEDIA:
                    holder.mRowTabHomeBinding.textDesc.setText(mContext.getString(R.string.label_photo));
                    break;
                case Utility.CHAT_TYPE_MESSAGE:
                    holder.mRowTabHomeBinding.textDesc.setText(model.message);
                    break;
            }

            holder.mRowTabHomeBinding.textDate.setText(FirebaseUtils.getTimeAgo(model.getTimestampLong(), mContext));

            if (model.chatId.equalsIgnoreCase(model.taskId)) //Multiple participants
            {
                holder.mRowTabHomeBinding.textName.setText(model.categoryName);
                holder.mRowTabHomeBinding.textParticipantCounter.setText(String.valueOf(model.totalParticipants));
                holder.mRowTabHomeBinding.textParticipantCounter.setVisibility(View.VISIBLE);
                holder.mRowTabHomeBinding.imgProfile.setVisibility(View.INVISIBLE);
                holder.mRowTabHomeBinding.textDesc.setText(model.taskDesc);
            } else //Single participants
            {
                Utility.showCircularImageView(holder.mRowTabHomeBinding.imgProfile.getContext(), TAG, holder.mRowTabHomeBinding.imgProfile, model.participantPhotoUrl, Utility.DEFAULT_CHEEP_LOGO);

                holder.mRowTabHomeBinding.textName.setText(model.participantName);
                holder.mRowTabHomeBinding.imgProfile.setVisibility(View.VISIBLE);
                holder.mRowTabHomeBinding.textParticipantCounter.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onChatItemClicked(model, holder.getAdapterPosition());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<TaskChatModel> getmList() {
        return mList;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowTabChatBinding mRowTabHomeBinding;

        public ViewHolder(RowTabChatBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowTabHomeBinding = binding;
        }
    }

    class ViewProgressHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowFooterProgressBinding rowFooterProgressBinding;

        public ViewProgressHolder(RowFooterProgressBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowFooterProgressBinding = binding;
        }
    }

    public interface ChatItemInteractionListener {
        void onChatItemClicked(TaskChatModel model, int position);
    }
}
