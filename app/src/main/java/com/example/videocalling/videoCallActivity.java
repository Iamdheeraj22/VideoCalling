package com.example.videocalling;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private final static String API_key="47231264";
    String callingId="",ringingId="";
    String senderUserId="";

    private final static String SESSION_ID="1_MX40NzIzMTI2NH5-MTYyMTI0ODU0MDE1Mn41RzA1Tk1SZ1ZXZ3ZLME9lZ3gvUUcxOTl-fg";
    private final static String TOKEN="T1==cGFydG5lcl9pZD00NzIzMTI2NCZzaWc9NTAwNDI0YjM1YTFlOGNlNzAxZTEzNTExMjY3OWY0Y2RjYzVjZWRlZjpzZXNzaW9uX2lkPTFfTVg0ME56SXpNVEkyTkg1LU1UWXlNVEkwT0RVME1ERTFNbjQxUnpBMVRrMVNaMVpYWjNaTE1FOWxaM2d2VVVjeE9UbC1mZyZjcmVhdGVfdGltZT0xNjIxMjQ4NTU4Jm5vbmNlPTAuOTA5MTQwNzY5MDkwNDM4MSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjIzODQwNTU3JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=videoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION=124;
    DatabaseReference usersRef;
    String UserId="";
    FloatingActionButton rotation,endVideoCall;
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
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {
        finish();
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
    public void onDisconnected(Session session){
        finish();
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
        if(mSubscriber !=null){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //sender side
        startActivity(new Intent(videoCallActivity.this,MainActivity.class));
        finish();
    }
}