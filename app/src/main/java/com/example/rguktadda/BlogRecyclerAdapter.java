package com.example.rguktadda;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.Placeholder;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter {

    public List<BlogPost> blogPostList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogPostList){

        this.blogPostList = blogPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list_item,viewGroup,false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        /*String desc_data = blogPostList.get(i).getDesc();
        viewHolder.setDescView(desc_data);*/
        ((ViewHolder)viewHolder).bindView(i);

    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private View mView;
        private TextView descView;
        private ImageView postImage;
        private CircleImageView profileThumbImage;
        private TextView postDate;
        private TextView userName;
        private ImageView likeButton;
        private TextView likeView;
        String currentUser;
        private String blogPostId;
        private ImageView blogCommentBtn;
        private TextView blogCommentsCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            descView = itemView.findViewById(R.id.blog_desc);
            postImage = itemView.findViewById(R.id.blog_image);
            profileThumbImage = itemView.findViewById(R.id.blog_user_image);
            postDate = itemView.findViewById(R.id.blog_date);
            userName = itemView.findViewById(R.id.blog_user_name);
            likeButton = itemView.findViewById(R.id.blog_like_btn);
            likeView = itemView.findViewById(R.id.blog_like_count);
            currentUser = firebaseAuth.getCurrentUser().getUid();
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            blogCommentsCount = mView.findViewById(R.id.blog_comment_count);

        }
        /*public void setDescView(String desc){

            descView = mView.findViewById( R.id.blog_desc);
            descView.setText(desc);
        }*/
        public void bindView(final int position){
            //imageView.setImageDrawable();
            descView.setText(blogPostList.get(position).getDesc());
            blogPostId = blogPostList.get(position).BlogPostId;

            RequestOptions requestOptions2 = new RequestOptions();
            requestOptions2.placeholder(R.drawable.common_google_signin_btn_icon_dark);
            Glide.with(context).applyDefaultRequestOptions(requestOptions2).load(blogPostList.get(position).getImage_url()).into(postImage);
            //Glide.with(context).load(blogPostList.get(position).getImage_thumb()).into(profileThumbImage);
            long millisecond = blogPostList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            postDate.setText(dateString);

            blogCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent commentIntent = new Intent(context, CommentsActivity.class);
                    commentIntent.putExtra("blog_post_id", blogPostId);
                    context.startActivity(commentIntent);

                }
            });

            firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if(!documentSnapshots.isEmpty()){

                        int count = documentSnapshots.size();

                        blogCommentsCount.setText(count+" Comments!");

                        // holder.updateLikesCount(count);

                    } else {

                        blogCommentsCount.setText(0+" Comments!");
                        // holder.updateLikesCount(0);

                    }

                }
            });

            final String userId = blogPostList.get(position).user_id;
            firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        String user_name = task.getResult().getString("name");
                        String userImage = task.getResult().getString("image");

                        userName.setText(user_name);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.common_google_signin_btn_icon_dark_focused);
                        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(userImage).into(profileThumbImage);
                    }else{

                    }
                }
            });

            //Get Likes Count
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if(!documentSnapshots.isEmpty()){

                        int count = documentSnapshots.size();

                        likeView.setText(count+" Likes!");

                       // holder.updateLikesCount(count);

                    } else {

                        likeView.setText(0+" Likes!");
                       // holder.updateLikesCount(0);

                    }

                }
            });

            //Get Likes
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if(documentSnapshot.exists()){

                        likeButton.setImageDrawable(context.getDrawable(R.drawable.common_google_signin_btn_icon_light));

                    } else {

                        likeButton.setImageDrawable(context.getDrawable(R.drawable.common_google_signin_btn_icon_dark_focused));

                    }

                }
            });

            //Likes Feature
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(!task.getResult().exists()){

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUser).set(likesMap);

                            } else {

                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUser).delete();

                            }

                        }
                    });
                }
            });



/*likeButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        HashMap<String,Object> likesMap = new HashMap<>();
        likesMap.put("timestamp", FieldValue.serverTimestamp());
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUser).set(likesMap);
    }
});*/

            /*Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);*/

        }

        @Override
        public void onClick(View v) {

            Toast.makeText(v.getContext(),"yuppp",Toast.LENGTH_SHORT).show();
        }
    }
}
