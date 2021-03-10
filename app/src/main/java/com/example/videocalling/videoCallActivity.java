package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class videoCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener
{
    private final static String API_key="47147804";
    private final static String SESSION_ID="2_MX40NzE0NzgwNH5-MTYxNDg1NjA1MzQwNH5HeGhLWnlyM2srQWZDUVhVTkRvcXRHUmp-fg";
    private final static String TOKEN="T1==cGFydG5lcl9pZD00NzE0NzgwNCZzaWc9NmUwMTM5N2NjMGE4Y2RmZTdlMTVhYzE4NzJjY2Q4OGFkMTJlNDc4MTpzZXNzaW9uX2lkPTJfTVg0ME56RTBOemd3Tkg1LU1UWXhORGcxTmpBMU16UXdOSDVIZUdoTFdubHlNMnNyUVdaRFVWaFZUa1J2Y1hSSFVtcC1mZyZjcmVhdGVfdGltZT0xNjE0ODU2MTI5Jm5vbmNlPTAuNDY5ODEzNTI0MzQxNTY2NjUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYxNzQ0NDUyNyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG=videoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION=124;
    DatabaseReference usersRef;
    ImageView endVideoCall;
    String UserId="";
    FloatingActionButton rotation;
    Boolean isRotate;
    //Drag and drop parameter
    float xd,yd;
    float xm,ym;
    float xDesc,yDesc;
    Session mSession;
    Publisher mPublisher;
    Subscriber mSubscriber;

    FrameLayout mPublisherController,mSubscriberController;
    @SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocall);
        initVIew();
        UserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        endVideoCall.setOnClickListener(v -> usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child(UserId).hasChild("Ringing"))
                {
                    usersRef.child(UserId).child("Ringing").removeValue();
                    if(mPublisher!=null){
                        mPublisher.destroy();
                    }
                    if(mSubscriber!=null){
                        mSubscriber.destroy();
                    }
                    startActivity(new Intent(videoCallActivity.this,StartActivity.class));
                    finish();
                }
                if(snapshot.child(UserId).hasChild("Calling"))
                {
                    usersRef.child(UserId).child("Calling").removeValue();
                    startActivity(new Intent(videoCallActivity.this,StartActivity.class));
                    finish();
                }else {
                    if(mPublisher!=null){
                        mPublisher.destroy();
                    }
                    if(mSubscriber!=null){
                        mSubscriber.destroy();
                    }
                    startActivity(new Intent(videoCallActivity.this,StartActivity.class));
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(videoCallActivity.this,error.toException().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }));
        // Drag and Drop
        mPublisherController.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int e=event.getActionMasked();
                switch (e)
                {
                    case MotionEvent.ACTION_DOWN:
                        xd= event.getX();
                        yd= event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xm=event.getX();
                        ym=event.getY();
                        xDesc=xm-xd;
                        yDesc=ym-yd;
                        mPublisherController.setX(mPublisherController.getX()+xDesc);
                        mPublisherController.setY(mPublisherController.getY()+yDesc);
                        break;
                }
                return true;
            }
        });
        rotation.setOnClickListener(v -> {
            if(!isRotate){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isRotate=true;
            }else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                isRotate=false;
            }
        });
        requestPermissions();
    }

    private void initVIew() {
        endVideoCall=findViewById(R.id.close_video_call_btn);
        mPublisherController=findViewById(R.id.publisher_container);
        mSubscriberController=findViewById(R.id.subscriber_container);
        rotation=findViewById(R.id.screen_rotate);
        isRotate=false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,videoCallActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private  void requestPermissions()
    {
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA};
        if(EasyPermissions.hasPermissions(this ,perms))
        {
            mSession=new Session.Builder(this,API_key,SESSION_ID).build();
            mSession.setSessionListener(videoCallActivity.this);
            mSession.connect(TOKEN);
        }else{
            EasyPermissions.requestPermissions(this,"Permissions Alert!(Audio and Camera permission)",RC_VIDEO_APP_PERMISSION,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher=new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(videoCallActivity.this);

        mPublisherController.addView(mPublisher.getView());
        if(mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"Stream Received");
        if(mSubscriber==null){
            mSubscriber=new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if(mSubscriber !=null)
        {
            mSubscriber=null;
            mSubscriberController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Error occured");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}