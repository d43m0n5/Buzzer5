package com.tech.abhinav.buzzer.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tech.abhinav.buzzer.Comment.CommentListActivity;
import com.tech.abhinav.buzzer.Profile.ProfileSeeActivity;
import com.tech.abhinav.buzzer.R;
import com.tech.abhinav.buzzer.Timeline.Asyncpost;
import com.tech.abhinav.buzzer.Timeline.HomeSingleActivity;
import com.tech.abhinav.buzzer.Timeline.MainActivity;
import com.tech.abhinav.buzzer.Utility.Home;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    FirebaseRecyclerAdapter<Home, HomeViewHolder> firebaseRecyclerAdapter;
    private RecyclerView mHomePage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCollege;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;
    private Query orderData;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mProcessLike = false;
    private LinearLayoutManager mLayoutManager;

    public View mMainView;

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_home , container ,false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(MainActivity.clgID).child("Post");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
        orderData = mDatabase.orderByChild("post_id").limitToFirst(30);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDatabase.keepSynced(true);
        orderData.keepSynced(true);
        mAuth=FirebaseAuth.getInstance();

        mHomePage = (RecyclerView) mMainView.findViewById(R.id.Home_Page);
        mHomePage.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Home, HomeViewHolder>(

                Home.class,
                R.layout.home_row,
                HomeViewHolder.class,
                orderData


        ) {


            @Override
            public int getItemViewType(int position) {

                Home obj = getItem(position );
                switch (obj.getHas_image()) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                }
                return super.getItemViewType(position);
            }

            @Override

            public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case 0:
                        View type1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_wihtout_image, parent, false);
                        return new HomeViewHolder(type1);
                    case 1:
                        View type2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_row, parent, false);
                        return new HomeViewHolder(type2);
                }

                return super.onCreateViewHolder(parent, viewType);
            }
            @Override
            protected void populateViewHolder(HomeViewHolder viewHolder, final Home model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setEvent(model.getEvent());
                viewHolder.setPost(model.getPost());
                if (model.getHas_image() == 1)
                    viewHolder.setImage(getContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setLikeButton(post_key);
                viewHolder.setProfile_Pic(getContext(), model.getProfile_pic());
                viewHolder.setPostTime(model.getPost_time());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this , "You clicked a view" , Toast.LENGTH_SHORT).show();

                        Intent singleHomeIntent = new Intent(getContext(), HomeSingleActivity.class);
                        singleHomeIntent.putExtra("home_id", post_key);
                        singleHomeIntent.putExtra("colgId", MainActivity.clgID);
                        startActivity(singleHomeIntent);
                    }
                });

                viewHolder.mCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(getContext(), CommentListActivity.class);
                        commentIntent.putExtra("home_id", post_key);
                        commentIntent.putExtra("colgId", MainActivity.clgID);
                        commentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(commentIntent);
                    }
                });

                viewHolder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getContext(), ProfileSeeActivity.class);
                        profileIntent.putExtra("home_id", post_key);
                        profileIntent.putExtra("colgId", MainActivity.clgID);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(profileIntent);
                    }
                });

                viewHolder.mUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getContext(), ProfileSeeActivity.class);
                        profileIntent.putExtra("home_id", post_key);
                        profileIntent.putExtra("colgId", MainActivity.clgID);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(profileIntent);
                    }
                });


                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;


                        mDatabaseLike.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(model.geteventId());

                                        mProcessLike = false;

                                    } else {
                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random Value");
                                        FirebaseMessaging.getInstance().subscribeToTopic(model.geteventId());
                                        JSONObject message =new JSONObject();
                                        try
                                        {
                                            message.put("to","/topics/"+model.geteventId());
                                            message.put("notification",new JSONObject()
                                            .put("title","New Notifications")
                                            .put("body","New Notifications"));
                                            Asyncpost asyncpost=new Asyncpost();
                                            asyncpost.execute(message);
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }


                                        mProcessLike = false;
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });


            }
        };
        mHomePage.setAdapter(firebaseRecyclerAdapter);



    }


    public static class HomeViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikeButton;
        ImageButton mCommentButton;

        CircleImageView mProfileImage;
        TextView mUserName;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        HomeViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mLikeButton = (ImageButton) mView.findViewById(R.id.likeButton);
            mCommentButton = (ImageButton) mView.findViewById(R.id.commentButton);
            mProfileImage = (CircleImageView) mView.findViewById(R.id.user_pic);
            mUserName = (TextView) mView.findViewById(R.id.postUsername);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
            mAuth = FirebaseAuth.getInstance();

            mDatabaseLike.keepSynced(true);
        }

        public void setLikeButton(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                        mLikeButton.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        mLikeButton.setImageResource(R.mipmap.ic_launcher_unlike);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setEvent(String event) {
            TextView post_event = (TextView) mView.findViewById(R.id.post_event);
            post_event.setText(event);

        }

        public void setPost(String post) {

            TextView post_text = (TextView) mView.findViewById(R.id.post_text);
            post_text.setText(post);


        }

        public void setImage(Context ctx, String image) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx).load(image).into(post_image);

        }

        public void setUsername(String username) {
            TextView post_username = (TextView) mView.findViewById(R.id.postUsername);
            post_username.setText(username);
        }

        public void setProfile_Pic(Context ctx, String image) {
            CircleImageView profile_pic = (CircleImageView) mView.findViewById(R.id.user_pic);
            Picasso.with(ctx).load(image).into(profile_pic);

        }

        public void setPostTime(String post_time) {
            TextView post_timeline = (TextView) mView.findViewById(R.id.timestamp);
            post_timeline.setText(post_time);
        }

    }
}
