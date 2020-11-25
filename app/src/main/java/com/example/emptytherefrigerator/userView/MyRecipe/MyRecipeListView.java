package com.example.emptytherefrigerator.userView.MyRecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emptytherefrigerator.AsyncTasks.MyAsyncTask;
import com.example.emptytherefrigerator.R;
import com.example.emptytherefrigerator.entity.RecipeIn;
import com.example.emptytherefrigerator.login.UserInfo;
import com.example.emptytherefrigerator.main.RecipeDetailCreateView;
import com.example.emptytherefrigerator.network.JsonParsing;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyRecipeListView extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ArrayList<RecipeIn> list = new ArrayList<>();
    private MyRecipeListAdapter adapter = null;

    @Override
    public void onRestart()
    {
        super.onRestart();
        // do some stuff here
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_my_recipe_list);
        getRecipeList();
        initializeView();
    }

    public void initializeView()
    {
        recyclerView = findViewById(R.id.recipeRecycler);
        adapter = new MyRecipeListAdapter(this, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        toolbar = findViewById(R.id.myRecipeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.my_recipe_list_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:     //뒤로가기 버튼이 눌렸을 때
                finish();
                break;
            case R.id.btnCreateRecipe:
                //레시피 등록 화면으로 넘어감
                Intent intent = new Intent(getApplicationContext(), RecipeDetailCreateView.class);      //현재 화면의 제어를 넘길 클래스 지정
                startActivity(intent);      //다음 화면으로 넘어감
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getRecipeList()        //내 레시피 서버에서 받아온다
    {
        MyAsyncTask recipeSearch = new MyAsyncTask();
        JSONObject data = new JSONObject();

        String jsonData="";
        try
        {
            data.accumulate("userId", UserInfo.getString(this,UserInfo.ID_KEY));
            jsonData = recipeSearch.execute("readUserRecipe", data.toString()).get();
            list = JsonParsing.parsingRecipe(jsonData);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
