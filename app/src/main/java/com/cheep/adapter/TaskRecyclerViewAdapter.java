package com.cheep.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.databinding.RowTaskBinding;
import com.cheep.databinding.RowTaskGroupBinding;
import com.cheep.databinding.RowUpcomingTaskBinding;
import com.cheep.fragment.TaskFragment;
import com.cheep.interfaces.TaskRowDataInteractionListener;
import com.cheep.model.BannerImageModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.strategicpartner.model.ServiceTaskDetailModel;
import com.cheep.utils.LoadMoreSwipeRecyclerAdapter;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 9/27/16.
 */
public class TaskRecyclerViewAdapter extends LoadMoreSwipeRecyclerAdapter<TaskRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = TaskRecyclerViewAdapter.class.getSimpleName();
    public static final int VIEW_TYPE_UPCOMING = 1;
    public static final int VIEW_TYPE_INDIVIDUAL = 2;
    public static final int VIEW_TYPE_GROUP = 3;

    private TaskRowDataInteractionListener listener;
    private Context context;

    ArrayList<TaskDetailModel> mList;
    int whichFrag;
    Calendar startDateTimeCalendar;
    SuperCalendar superStartDateTimeCalendar;
    private int mLiveIconOffset;

//    //saves the index of the upcoming offer to display
//    private Map<String, Integer> mOfferIndexMap;


    /*private Uri[] urls = new Uri[]{Uri.parse("http://www.animated-gifs.eu/category_leisure/avatars-100x100-music/0016.gif"), Uri.parse("http://www.smailikai.com/avatar/skelet/avatar_4348.gif"), Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png")*//*, Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png"), Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png")*//*};
    private List<Uri> arrayListUri=new ArrayList<>();
    String[] stringArr=new String[]{"3.810 plumbing task booked today","17 home made happy by Lokesh Shah today.","Favorited by 9,800 Users till now"};*/

    public TaskRecyclerViewAdapter(Context mContext, int whichFrag, TaskRowDataInteractionListener listener) {
        this.context = mContext;
        this.mList = new ArrayList<>();
        this.whichFrag = whichFrag;
        this.listener = listener;
        int offset = context.getResources().getDimensionPixelSize(R.dimen.scale_4dp);
        mLiveIconOffset = context.getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;
        //arrayListUri.addAll(Arrays.asList(urls));
//        mOfferIndexMap = new HashMap<>();
    }

    /*public TaskRecyclerViewAdapter(ArrayList<TaskDetailModel> mList, TaskRowDataInteractionListener listener, int whichFrag) {
        this.mList = mList;
        this.listener = listener;
        this.whichFrag = whichFrag;
    }*/

    public void setItem(ArrayList<TaskDetailModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<TaskDetailModel> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getActualItemViewType(int position) {
        if (whichFrag == TaskFragment.TAB_PAST_TASK) {
            if (mList.get(position).taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                return VIEW_TYPE_INDIVIDUAL;
            } else {
                if (mList.get(position).selectedProvider == null) {
                    return VIEW_TYPE_GROUP;
                } else {
                    return VIEW_TYPE_INDIVIDUAL;
                }
            }
        } else {
            return VIEW_TYPE_UPCOMING;
        }
    }


    @Override
    public ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        startDateTimeCalendar = Calendar.getInstance();
        superStartDateTimeCalendar = SuperCalendar.getInstance();
        if (viewType == VIEW_TYPE_UPCOMING) {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_upcoming_task, parent, false);
            return new ViewHolder(mRowTaskBinding);
        } else if (viewType == VIEW_TYPE_GROUP) {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task_group, parent, false);
            return new ViewHolder(mRowTaskBinding);
        } else {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task, parent, false);
            return new ViewHolder(mRowTaskBinding);
        }
    }

    @Override
    public void onActualBindViewHolder(final ViewHolder holder, final int position) {
        final TaskDetailModel model = mList.get(holder.getAdapterPosition());

        holder.removeAnimations();

        int viewType = getItemViewType(holder.getAdapterPosition());
        switch (viewType) {
            case VIEW_TYPE_UPCOMING:
                superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
                superStartDateTimeCalendar.setLocaleTimeZone();
            /*if(model.live_lable_arr.size()==0)
                model.live_lable_arr.addAll(Arrays.asList(stringArr));*/

                // Start LIVE tracking and Text changes
                final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
                final Map<String, Integer> mOfferIndexMap = new HashMap<>();
                if (liveFeedCounter > 0) {
                    holder.mUpcomingTaskBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.tvLiveFeed.setVisibility(View.VISIBLE);

                    // Start live image animations
                    holder.mUpcomingTaskBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
                    ((AnimationDrawable) holder.mUpcomingTaskBinding.ivLiveAnimated.getBackground()).start();

                    AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.mUpcomingTaskBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            int offerIndex = mOfferIndexMap.containsKey(model.taskId) ? mOfferIndexMap.get(model.taskId) : 0;
                            if (offerIndex == model.live_lable_arr.size()) {
                                Log.i(TAG, "onAnimationEnd: Issue Caught Here>>>>>>>>>>>>>>>>>>>");
                                return;
                            }
                            SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                            labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            holder.mUpcomingTaskBinding.tvLiveFeed.setText(labelOffer);
                            offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                            mOfferIndexMap.put(model.taskId, offerIndex);
                        }
                    });
                    offerAnimation.start();
                    holder.addAnimator(offerAnimation);
                    int offerIndex = mOfferIndexMap.containsKey(model.taskId) ? mOfferIndexMap.get(model.taskId) : 0;
                    SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    holder.mUpcomingTaskBinding.tvLiveFeed.setText(labelOffer);
                    offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(model.taskId, offerIndex);
                } else {
                    holder.mUpcomingTaskBinding.ivLiveAnimated.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.tvLiveFeed.setVisibility(View.GONE);
                }


                holder.mUpcomingTaskBinding.gridImageView.clear();
                //holder.mUpcomingTaskBinding.gridImageView.createWithUrls(arrayListUri); // just for testing
                holder.mUpcomingTaskBinding.gridImageView.createWithUrls(getURIListFromStringList(model.profile_img_arr));

                if (model.selectedProvider == null) {
                    holder.mUpcomingTaskBinding.layoutIndividualProfile.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.layoutGroupProfile.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.tvProviderName.setText(model.categoryName);
                    holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.ratingBar.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.imgBadge.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.tvViewTask.setVisibility(View.GONE);
                    holder.mUpcomingTaskBinding.tvViewQuotes.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.tvTaskResponseStatus.setVisibility(View.VISIBLE);
                    // Need to hide reschedule button as PRO is not Finalized now.
                    holder.mUpcomingTaskBinding.frameRescheduleTask.setVisibility(View.GONE);
                } else {
                    holder.mUpcomingTaskBinding.layoutIndividualProfile.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.layoutGroupProfile.setVisibility(View.GONE);

                    holder.mUpcomingTaskBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            listener.onFavClicked(model, !holder.mUpcomingTaskBinding.imgFav.isSelected(), position);
                            holder.mUpcomingTaskBinding.imgFav.setSelected(!holder.mUpcomingTaskBinding.imgFav.isSelected());

                        }
                    });

                    if (model.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                        holder.mUpcomingTaskBinding.tvProviderName.setText(model.categoryName);
                        holder.mUpcomingTaskBinding.imgFav.setVisibility(View.GONE);


                    } else {
                        holder.mUpcomingTaskBinding.tvProviderName.setText(model.selectedProvider.userName);
                        holder.mUpcomingTaskBinding.imgFav.setVisibility(View.VISIBLE);
                        if (Utility.BOOLEAN.YES.equals(model.selectedProvider.isFavourite))
                            holder.mUpcomingTaskBinding.imgFav.setSelected(true);
                        else
                            holder.mUpcomingTaskBinding.imgFav.setSelected(false);
                    }
                    Utility.showCircularImageViewWithColorBorder(holder.mUpcomingTaskBinding.imgProfilePic.getContext(), TAG, holder.mUpcomingTaskBinding.imgProfilePic, model.selectedProvider.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.grey_dark_color, true);

                    // Show Rating
                    holder.mUpcomingTaskBinding.ratingBar.setVisibility(View.VISIBLE);
                    Utility.showRating(model.selectedProvider.rating, holder.mUpcomingTaskBinding.ratingBar);

                    holder.mUpcomingTaskBinding.imgBadge.setVisibility(View.VISIBLE);


                    if (model.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                        holder.mUpcomingTaskBinding.imgBadge.setImageResource(R.drawable.ic_silver_badge_partner);
                        holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.VISIBLE);
                        holder.mUpcomingTaskBinding.tvVerified.setText(context.getString(R.string.label_partner));
                    } else {

                        if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified)) {
                            holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.VISIBLE);
                            holder.mUpcomingTaskBinding.tvVerified.setText(context.getString(R.string.label_verified).toLowerCase());
                        } else {
                            holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.GONE);
                        }

                        int bagResId = Utility.getProLevelBadge(model.selectedProvider.pro_level);
                        if (bagResId != -1)
                            holder.mUpcomingTaskBinding.imgBadge.setImageResource(bagResId);

                    }
                    holder.mUpcomingTaskBinding.tvViewTask.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.tvViewQuotes.setVisibility(View.GONE);

                    holder.mUpcomingTaskBinding.tvTaskResponseStatus.setVisibility(View.GONE);

                    // Need to Show reschedule button as PRO is not Finalized now.
                    holder.mUpcomingTaskBinding.frameRescheduleTask.setVisibility(View.VISIBLE);
                }

//            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.mUpcomingTaskBinding.tvVerified.getLayoutParams();
//            layoutParams.setMargins(holder.mUpcomingTaskBinding.tvProviderName.getWidth(), 0, 0, 0);
//            holder.mUpcomingTaskBinding.tvVerified.setLayoutParams(layoutParams);


                /**
                 * For the bug for lower version of Device like Kitkat, we have to store the padding before setting the background of textview,
                 * as due to bug it would reset the padding once resource set to the textview.
                 */
                int pL = holder.mUpcomingTaskBinding.tvViewQuotes.getPaddingLeft();
                int pT = holder.mUpcomingTaskBinding.tvViewQuotes.getPaddingTop();
                int pR = holder.mUpcomingTaskBinding.tvViewQuotes.getPaddingRight();
                int pB = holder.mUpcomingTaskBinding.tvViewQuotes.getPaddingBottom();
                if (model.providerCount.equals(Utility.ZERO_STRING)) {
                    holder.mUpcomingTaskBinding.tvTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_awaiting_response));
                    holder.mUpcomingTaskBinding.tvViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_grey_rounded));
                    holder.mUpcomingTaskBinding.tvViewQuotes.setEnabled(false);
                    holder.mUpcomingTaskBinding.tvViewQuotes.setPadding(pL, pT, pR, pB);
                } else {
                    int providerCount = Integer.parseInt(model.providerCount);
                    holder.mUpcomingTaskBinding.tvTaskResponseStatus.setText(holder.mView.getContext().getResources().getQuantityText(R.plurals.getResponseReceivedString, providerCount));
                    holder.mUpcomingTaskBinding.tvViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_blue_rounded));
                    holder.mUpcomingTaskBinding.tvViewQuotes.setEnabled(true);
                    holder.mUpcomingTaskBinding.tvViewQuotes.setPadding(pL, pT, pR, pB);
                }

                holder.mUpcomingTaskBinding.tvSubCategoryName.post(new Runnable() {
                    @Override
                    public void run() {
                        if (model.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                            holder.mUpcomingTaskBinding.tvSubCategoryNameStrPart.setText(getAllUniqueCategories(model.subSubCategoryList));
                            holder.mUpcomingTaskBinding.tvSubCategoryNameStrPart.setVisibility(View.VISIBLE);
                            holder.mUpcomingTaskBinding.tvSubCategoryName.setVisibility(View.GONE);
//                        holder.mUpcomingTaskBinding.tvSubCategoryNameStrPart.editMore();
                        } else {
                            holder.mUpcomingTaskBinding.tvSubCategoryName.setText(model.subCategoryName);
                            holder.mUpcomingTaskBinding.tvSubCategoryNameStrPart.setVisibility(View.GONE);
                            holder.mUpcomingTaskBinding.tvSubCategoryName.setVisibility(View.VISIBLE);
                        }
                    }
                });

                holder.mUpcomingTaskBinding.tvDesc.setText(model.taskDesc);

                final String mBookingDate = holder.mView.getContext().getString(R.string.format_task_book_date
                        , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM) + " " + Utility.get2HourTimeSlots(model.taskStartdate));
                holder.mUpcomingTaskBinding.tvTaskBookedDateTime.setText(mBookingDate);

                holder.mUpcomingTaskBinding.tvTaskStartedTime.setText(Utility.getDateDifference(holder.mView.getContext(), superStartDateTimeCalendar.format(Utility.DATE_FORMAT_FULL_DATE)));
                holder.mUpcomingTaskBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus) || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)) {
                            Utility.showToast(context, context.getString(R.string.msg_processing_task_cancelled));
                            holder.mUpcomingTaskBinding.swipeLayout.close(true);
                        } else {
                            listener.onTaskDelete(whichFrag, model, holder.mUpcomingTaskBinding);
                        }
                    }
                });

                holder.mUpcomingTaskBinding.textReschedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus)
                                || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)
                                || Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(model.taskStatus)) {
                            Utility.showToast(context, context.getString(R.string.msg_processing_task_reschduled));
                            holder.mUpcomingTaskBinding.swipeLayout.close(true);
                        } else {
                            listener.onTaskReschedule(whichFrag, model, holder.mUpcomingTaskBinding);
                        }
                    }
                });

                holder.mUpcomingTaskBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onTaskDelete(whichFrag, model, holder.mUpcomingTaskBinding);
                    }
                });

                holder.mUpcomingTaskBinding.tvViewQuotes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onViewQuotesClick(whichFrag, model);
                    }
                });

                holder.mUpcomingTaskBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onTaskRowFragListItemClicked(whichFrag, model);
                    }
                });
                //So swipe adapter (lib method) close any previous opened swipe menu when current swipe is done.
                holder.mUpcomingTaskBinding.swipeLayout.setSwipeEnabled(true);
                mItemManger.bindView(holder.itemView, position);

                break;
            case VIEW_TYPE_GROUP: {
                superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
                superStartDateTimeCalendar.setLocaleTimeZone();

                String date_time = holder.mView.getContext().getString(R.string.format_date_time
                        , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                        , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));

                holder.mRowTaskGroupBinding.textDateTime.setText(date_time);

                holder.mRowTaskGroupBinding.textDesc.setText(model.taskDesc);
                holder.mRowTaskGroupBinding.textCategoryName.setText(model.categoryName);
                holder.mRowTaskGroupBinding.textSubCategoryName.setText(model.subCategoryName);

                if (model.providerCount.equals("0")) {
                    holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(model.providerCount));
                    holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_responses));
                } else {
                    int providerCount = Integer.parseInt(model.providerCount);
                    if (providerCount == 1) {
                        holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(providerCount));
                    } else {
                        holder.mRowTaskGroupBinding.textResponseCounter.setText("+" + String.valueOf(providerCount - 1));
                    }
                    holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_responses));
                }
                if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                    holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
                    holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));


                } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                    holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                    holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
                } else if (Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                    holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_lapsed));
                    holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
                } else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                    holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
                    holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
                } else {
                    holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.GONE);
                }
                holder.mRowTaskGroupBinding.swipeLayout.setSwipeEnabled(false);
                mItemManger.bindView(holder.itemView, position);
                break;
            }
            default: {
                //======Individual item(when sp is selected for task)=====
                superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
                superStartDateTimeCalendar.setLocaleTimeZone();

                String date_time = holder.mView.getContext().getString(R.string.format_date_time
                        , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                        , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
                holder.mRowTaskBinding.textDateTime.setText(date_time);

                holder.mRowTaskBinding.textDesc.setText(model.taskDesc);

                if (model.selectedProvider != null) {
                    Utility.showCircularImageViewWithColorBorder(holder.mRowTaskBinding.imgProfile.getContext(), TAG, holder.mRowTaskBinding.imgProfile, model.selectedProvider.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.grey_dark_color, true);

                    holder.mRowTaskBinding.textTaskApprovedQuote.setText(holder.mRowTaskBinding.imgProfile.getContext().getString(R.string.rupee_symbol_x_space, Utility.getActualPrice(model.taskPaidAmount, model.selectedProvider.quotePrice)));
                    if (model.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                        holder.mRowTaskBinding.textProviderName.setText(model.categoryName);
                        holder.mRowTaskBinding.imgBadge.setVisibility(View.INVISIBLE);
                        holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
                        holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_partner));
                    } else {
                        holder.mRowTaskBinding.imgBadge.setVisibility(View.VISIBLE);
                        holder.mRowTaskBinding.textProviderName.setText(model.selectedProvider.userName);
                        holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
                        holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_verified).toLowerCase());
                        int bagResId = Utility.getProLevelBadge(model.selectedProvider.pro_level);
                        if (bagResId != -1)
                            holder.mRowTaskBinding.imgBadge.setImageResource(bagResId);
                    }
                    if (!TextUtils.isEmpty(model.subCategoryName))
                        holder.mRowTaskBinding.textSubCategoryName.setText(model.subCategoryName);
                    else
                        holder.mRowTaskBinding.textSubCategoryName.setText(getAllUniqueCategories(model.subSubCategoryList));

                    holder.mRowTaskBinding.imgProfile.setVisibility(View.VISIBLE);

//                   if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified)) {
//                       holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
//                       holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_verified).toLowerCase());
//                   } else {
//                       holder.mRowTaskBinding.textVerified.setVisibility(View.GONE);
//                   }

                    // Show Rating
                    Utility.showRating(model.selectedProvider.rating, holder.mRowTaskBinding.ratingBar);
                } else {

                    Utility.showCircularImageView(holder.mRowTaskBinding.imgProfile.getContext(), TAG, holder.mRowTaskBinding.imgProfile, "", Utility.DEFAULT_PROFILE_SRC);
                    holder.mRowTaskBinding.textTaskApprovedQuote.setText(holder.mRowTaskBinding.imgProfile.getContext().getString(R.string.rupee_symbol_x_space, Utility.getActualPrice("", "")));

                    holder.mRowTaskBinding.textProviderName.setText("");
                    holder.mRowTaskBinding.imgBadge.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textVerified.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textSubCategoryName.setText("");

//                   if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified)) {
//                       holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
//                       holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_verified).toLowerCase());
//                   } else {
//                       holder.mRowTaskBinding.textVerified.setVisibility(View.GONE);
//                   }

                    // Show Rating
                    Utility.showRating("0", holder.mRowTaskBinding.ratingBar);

                }
                holder.mRowTaskBinding.swipeLayout.close(true);

                holder.mRowTaskBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);

                if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus)) {
//                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.VISIBLE);
                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.GONE);

                } else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_reschedule_rejected));

//                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.VISIBLE);
//                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.VISIBLE);
//                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.VISIBLE);

                } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus)) {
//                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));

                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.VISIBLE);
                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.GONE);

                } else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
                } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(model.taskStatus)) {
//                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));

                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.VISIBLE);
                } else if (Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(model.taskStatus)) {
//                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));

                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.VISIBLE);
                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.GONE);

                } else {
//                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));

                    holder.mRowTaskBinding.textTaskStatusCancelled.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusElapsed.setVisibility(View.GONE);
                    holder.mRowTaskBinding.textTaskStatusCompleted.setVisibility(View.VISIBLE);
                    holder.mRowTaskBinding.textTaskStatus.setText("");
                }
                holder.mRowTaskBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onTaskRowFragListItemClicked(whichFrag, model);
                    }
                });

                holder.mRowTaskBinding.textBookSimilarTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        @Nullable BannerImageModel bannerImageModel;
                        @Nullable JobCategoryModel jobCategoryModel;
                        if (model.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                            jobCategoryModel = null;

                            bannerImageModel = new BannerImageModel();
                            bannerImageModel.imgCatImageUrl = model.catImageExtras.medium;
                            bannerImageModel.bannerImage = model.bannerImage;
                            bannerImageModel.cat_id = model.categoryId;
                            bannerImageModel.name = model.categoryName;
                            bannerImageModel.minimum_selection = "1";

                        } else {

                            jobCategoryModel = new JobCategoryModel();
                            jobCategoryModel.catId = model.categoryId;
                            jobCategoryModel.catName = model.categoryName;
                            jobCategoryModel.catImage = model.catImage;
                            jobCategoryModel.catImageExtras = model.catImageExtras;

                            bannerImageModel = null;
                        }
                        listener.onBookSimilarTaskClicked(jobCategoryModel, bannerImageModel);

                    }
                });
                holder.mRowTaskBinding.swipeLayout.setSwipeEnabled(false);
                mItemManger.bindView(holder.itemView, position);
                break;
            }
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    public ArrayList<TaskDetailModel> getmList() {
        return mList;
    }

    public void updateFavStatus(String id, String isFav) {
        if (mList != null) {
            boolean isUpdated = false;
            //we are not breaking this loop because we may need to change status of same selectedProvider in another rows
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.selectedProvider != null && providerModel.selectedProvider.providerId.equalsIgnoreCase(id)) {
                    providerModel.selectedProvider.isFavourite = isFav;
                    isUpdated = true;
//                    break;
                }
            }
            if (isUpdated)
                notifyDataSetChanged();
        }
    }

    public void updateRatedStatus(String id) {
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(id)) {
                    providerModel.ratingDone = Utility.BOOLEAN.YES;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    // Update the list in case new quote requested by any of the PRO
    public void updateOnNewQuoteRequested(String task_id, String max_quote_price, String sp_counts, String quoted_sp_image_url) {
        Log.d(TAG, "updateOnNewQuoteRequested() called with: task_id = [" + task_id + "], max_quote_price = [" + max_quote_price + "], sp_counts = [" + sp_counts + "], quoted_sp_image_url = [" + quoted_sp_image_url + "]");
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(task_id)) {
                    providerModel.maxQuotePrice = max_quote_price;
                    providerModel.providerCount = sp_counts;
                    if (providerModel.profile_img_arr == null)
                        providerModel.profile_img_arr = new ArrayList<>();
                    // Only add if already not added.
                    if (quoted_sp_image_url != null && !providerModel.profile_img_arr.contains(quoted_sp_image_url)) {
                        providerModel.profile_img_arr.add(quoted_sp_image_url);
                    } else {
                        Log.i(TAG, "updateOnNewQuoteRequested: Image URL Already added");
                    }
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public int cancelTask(String id, String status) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(id)) {
                    taskDetailModel.taskStatus = status;
                    mList.remove(i);
                    notifyItemRemoved(i);

                    //Need to call listener for getting callback for listener
                    listener.onMigrateTaskFromPendingToPast(taskDetailModel);
                    break;
                }
                i++;
            }
        }
        return mList.size();
    }

    public void addData(TaskDetailModel model) {
        mList.add(model);
        notifyDataSetChanged();
    }

    public void rescheduleTask(String taskId, String start_datetime) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(taskId)) {
//                    taskDetailModel.taskStartdate = start_datetime;
                    if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                        taskDetailModel.taskStartdate = start_datetime;
                    } else {
                        taskDetailModel.taskStatus = Utility.TASK_STATUS.RESCHEDULE_REQUESTED;
                    }
                    notifyItemChanged(i);
                    break;
                }
                i++;
            }
        }
    }

    public void updateTaskStatus(MessageEvent event) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.taskStatus = event.taskStatus;
                    notifyItemChanged(i);
                    Log.i(TAG, "updateTaskStatus: Task Status Got Changed!!");
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnAdditionalPaymentRequested(MessageEvent event) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.taskStatus = event.taskStatus;
                    taskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
                    notifyItemChanged(i);
                    Log.i(TAG, "updateTaskStatus: Task Status Got Changed!!");
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnDetailRequestRejected(MessageEvent event) {
        Log.d(TAG, "updateOnDetailRequestRejected() called with: event = [" + event + "]");
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.profile_img_arr.remove(event.quoted_sp_image_url);
                    taskDetailModel.providerCount = String.valueOf(taskDetailModel.profile_img_arr.size());
                    notifyItemChanged(i);
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnNewDetailRequested(String id, String sp_counts, String quoted_sp_image_url) {
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(id)) {
                    providerModel.providerCount = sp_counts;
                    if (providerModel.profile_img_arr == null) {
                        providerModel.profile_img_arr = new ArrayList<>();
                    }
                    // Only add if already not added.
                    if (!providerModel.profile_img_arr.contains(quoted_sp_image_url)) {
                        providerModel.profile_img_arr.add(quoted_sp_image_url);
                    } else {
                        Log.i(TAG, "updateOnNewDetailRequested: Image URL Already added");
                    }
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    private AnimatorSet loadBannerScrollAnimation(View view, int offset, int distance, AnimatorListenerAdapter midEndListener) {
        ObjectAnimator moveOut = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, (-1 * distance));
        if (midEndListener != null) {
            moveOut.addListener(midEndListener);
        }

        ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, distance, 0);
        final AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.setStartDelay(offset);
        set.playSequentially(moveOut, moveIn);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                set.start();
            }
        });
        return set;
    }

    private List<Uri> getURIListFromStringList(List<String> imageUrls) {
        List<Uri> uriList = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                if (url != null && !url.isEmpty())
                    uriList.add(Uri.parse(url));
            }
        }
        return uriList;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        RowTaskBinding mRowTaskBinding;
        RowTaskGroupBinding mRowTaskGroupBinding;
        RowUpcomingTaskBinding mUpcomingTaskBinding;
        private int liveFeedindex = 0;

        private List<AnimatorSet> animators = new ArrayList<>();

        public void addAnimator(AnimatorSet animator) {
            if (animator != null) {
                animators.add(animator);
            }
        }

        public void removeAnimations() {
            for (AnimatorSet animatorSet : animators) {
                for (Animator child : animatorSet.getChildAnimations()) {
                    child.removeAllListeners();
                }
                animatorSet.removeAllListeners();
                animatorSet.end();
                animatorSet.cancel();
            }
            animators.clear();
        }

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            if (binding instanceof RowUpcomingTaskBinding) {
                mUpcomingTaskBinding = (RowUpcomingTaskBinding) binding;
            } else if (binding instanceof RowTaskBinding) {
                mRowTaskBinding = (RowTaskBinding) binding;
            } else {
                mRowTaskGroupBinding = (RowTaskGroupBinding) binding;
            }

        }
    }

    private String getAllUniqueCategories(List<ServiceTaskDetailModel> list) {
        if (list == null)
            return "";
        List<String> enemyIds = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (ServiceTaskDetailModel entry : list) {
            if (!enemyIds.contains(entry.subCategoryName)) {
                enemyIds.add(entry.subCategoryName);
                if (sb.toString().isEmpty())
                    sb.append(entry.subCategoryName);
                else
                    sb.append(", ").append(entry.subCategoryName);
            }
        }
        return sb.toString();
    }

    private void setLabelAfterEllipsis(final TextView textView, final String label) {
        ViewTreeObserver vto = textView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    Log.i(TAG, "showLess: ");
                    Layout layout = textView.getLayout();
                    int lineEndIndex = layout.getLineEnd(0);
                    CharSequence ELLIPSIZE = "...";
//                    CharSequence MORE= label;
                    String newText = textView.getText().toString().substring(0, lineEndIndex - (ELLIPSIZE.length() + label.length() + 1)) + ELLIPSIZE + label;
//                    SpannableStringBuilder builder = new SpannableStringBuilder(newText);
//       /* builder.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                showMore();
//            }
//        }, newText.length() - MORE.length(), newText.length(), 0);*/
//                    builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(textView.getContext(), R.color.splash_gradient_end)), newText.length() - MORE.length(), newText.length(), 0);
//                    builder.setSpan(new StyleSpan(Typeface.BOLD), newText.length() - MORE.length(), newText.length(), 0);
                    textView.setText(newText);
                } catch (Exception mException) {
                    Log.d(TAG, "onGlobalLayout() returned: " + "Exception");
                }
            }
        });


    }

    private int getTextWidth(String text, float textSize) {
        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, text.length(), bounds);

        int width = (int) Math.ceil(bounds.width());
        return width;
    }
}
