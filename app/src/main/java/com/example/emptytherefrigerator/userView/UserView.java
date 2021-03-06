package com.example.emptytherefrigerator.userView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.emptytherefrigerator.R;
import com.example.emptytherefrigerator.login.LoginView;
import com.example.emptytherefrigerator.login.UserInfo;
import com.example.emptytherefrigerator.main.MainPageView;
import com.example.emptytherefrigerator.userView.MyComment.MyCommentView;
import com.example.emptytherefrigerator.userView.MyLike.LikeMainPage;
import com.example.emptytherefrigerator.userView.MyRecipe.MyRecipeListView;

//회원정보 창
public class UserView extends Fragment
{
    private ImageButton btnEditUser, btnRecipeInquire, btnCommentInquire, btnLIkeInquire, btnSetting;
    private Button logoutBtn;
    private View view;
    private TextView userIdTextView, userPwTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.user_menu,container,false);
        initializeView();
        setListener();
        setUserInfo();
        return view;
    }
    public void initializeView()
    {
        userIdTextView = view.findViewById(R.id.userIdTextView);
        userPwTextView = view.findViewById(R.id.userPwTextView);
        btnEditUser = view.findViewById(R.id.memberEditBtn);
        btnRecipeInquire = view.findViewById(R.id.recipeInquireBtn);
        btnCommentInquire = view.findViewById(R.id.commentInquireBtn);
        btnLIkeInquire = view.findViewById(R.id.likeInquireBtn);
        btnSetting=view.findViewById(R.id.settingBtn);
        logoutBtn= view.findViewById(R.id.logoutBtn);
    }
    public void setListener()
    {
        btnEditUser.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {       //회원 수정 화면을 넘어감
                    Intent intent = new Intent(getActivity(), UserEditView.class);
                    startActivity(intent);
                }
            });
        btnRecipeInquire.setOnClickListener(new View.OnClickListener()
        {
                    @Override
                    public void onClick(View v)
                    {       //내 레시피 조회 화면으로 넘어감
                        Intent intent = new Intent(getActivity(), MyRecipeListView.class);
                        startActivity(intent);
                    }
                });
        btnCommentInquire.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {   //내 댓글 조회 화면으로 넘어감
                Intent intent = new Intent(getActivity(), MyCommentView.class);
                startActivity(intent);
            }
        });
        btnLIkeInquire.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {       //내 좋아요 조회
                Intent intent = new Intent(userIdTextView.getContext(), LikeMainPage.class);
                startActivity(intent);
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener()    //셋팅 화면 넘김
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), SettingView.class);
                startActivity(intent);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener()     ////로그인 화면으로 돌아감
        {
            @Override
            public void onClick(View v)
            {           //로그아웃
                Intent intent = new Intent(getActivity(), LoginView.class);
                intent.putExtra("logOut", "logOut");
                startActivity(intent);
            }
        });
    }
    public void setUserInfo()
    {
        String id = UserInfo.getString(getActivity(),UserInfo.ID_KEY);
        String pw = UserInfo.getString(getActivity(), UserInfo.PW_KEY);

        if(!id.equals("") || !pw.equals(""))        //shared preference에 값이있다면 가져옴
        {
            userIdTextView.setText(id);
            userPwTextView.setText(pw);
        }
    }
}
