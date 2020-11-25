package com.example.emptytherefrigerator.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emptytherefrigerator.AsyncTasks.RecipeMngAsyncTask;
import com.example.emptytherefrigerator.R;
import com.example.emptytherefrigerator.entity.RecipeIn;
import com.example.emptytherefrigerator.login.LoginView;
import com.example.emptytherefrigerator.login.UserInfo;
import com.example.emptytherefrigerator.userView.MyRecipe.MyRecipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

//레시피 등록 화면
//등록버튼으로 데이터 서버에 전송 후 완료 메시지를 빋아온다.
public class RecipeDetailCreateView extends AppCompatActivity {

    private EditText editTextTitle;

    private ImageView recipeDetailImage;
    private int recipeDetailImageIdx;
    private boolean isDetail;

    private EditText recipeInfoCount;
    private EditText recipeInfoTime;

    private LinearLayout recipeIngredientLayout;
    private LinearLayout recipeContentLayout;

    private ImageButton btnIngredient;          //재료 edittext 추가 버튼
    private ImageButton btnRemoveIngredient;
    private ImageButton btnRecipe;
    private ImageButton btnRemoveRecipe;
    private int ingredientCnt, recipeCnt;

    private List<EditText> etIngredientList;        //재료 리스트
    private List<EditText> etIngredientUnitList;    //재료단위 리스트
    private List<Spinner> spIngredientUnitList;     //ex)개, kg
    private List<TextView> recipeCountList;
    private List<ImageView> recipeImageViewList;    //요리 방법 이미지 리스트 ( 첫 이미지 경로는 대표이미지 경로 두번째 부터 요리방법 이미지 )
    private List<EditText> recipeContentList;       //요리 방법 설명 editText 리스트

    private Button btnRegister;
    private Button btnCancel;

    private Uri mImageCaptureUri;
    private String absoultePath ="";

    private int recipeContCnt;                  //요리방법 갯수 유지해야됨
    private RecipeIn recipe;                      //레시피 클래스 객체
    private String ingredient = "";
    private String ingredientUnit ="";
    private Bitmap repPhotoBitmap;
    private int imageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_detail_create_view);

        initialize();

        setListener();
    }
/////////////////////////////////////////////////////////////////////////

    public void initialize()
    {
        isDetail = false;
        recipeDetailImageIdx = -1;
        editTextTitle=(EditText) findViewById(R.id.editTextTitle);
        //대표 이미지
        //이미지뷰를 눌러 이미지 선택하면 옆에 선택한 이미지 생성
        recipeDetailImage = (ImageView) findViewById(R.id.recipeDetailImage);
        recipeInfoCount = (EditText)findViewById(R.id.recipeInfoCount);
        recipeInfoTime = (EditText)findViewById(R.id.recipeInfoTime);
        //레이아웃
        recipeIngredientLayout = (LinearLayout)findViewById(R.id.recipeIngredientLayout);
        recipeContentLayout = (LinearLayout)findViewById(R.id.recipeContentLayout);
        //추가버튼
        btnIngredient = (ImageButton) findViewById(R.id.btnIngredient);
        btnRemoveIngredient = (ImageButton)findViewById(R.id.btnRemoveIngredient);
        btnRecipe = (ImageButton) findViewById(R.id.btnRecipe);
        btnRemoveRecipe = (ImageButton) findViewById(R.id.btnRemoveRecipe);
        //식재료와 단위 저장 리스트
        etIngredientList = new ArrayList<EditText>();
        etIngredientUnitList = new ArrayList<EditText>();
        spIngredientUnitList = new ArrayList<Spinner>();
        recipeCountList = new ArrayList<TextView>();
        recipeImageViewList = new ArrayList<ImageView>();
        recipeContentList = new ArrayList<EditText>();
        //등록 취소 버튼
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        //요리 방법
        recipeContCnt = 0;

        recipe = new RecipeIn();

        //대표이미지뷰를 리스트에 추가
        recipeImageViewList.add(recipeDetailImage);
        //권한 체크
        permissionCheck();

        //재료 첫 번째 레이아웃
        setIngredient();
        ingredientCnt = 1;
        //요리 방법 첫번째 레이아웃
        setRecipeContent();
        recipeCnt = 1;
    }

    public void setListener()
    {
        recipeDetailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDetailImage();
            }
        });
        //재료 추가 버튼
        btnIngredient.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ingredientCnt++ < 20) {
                    if(!ingredientIsEmpty())
                    setIngredient();
                }else {
                    Toast.makeText(v.getContext(),"더 이상 늘릴 수 없습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //재료 삭제 버튼
        btnRemoveIngredient.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popIngredient();
            }
        });
        //요리 방법 추가 버튼
        btnRecipe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recipeCnt++ < 20) {
                    if(!recipeValueIsEmpty())
                    setRecipeContent();
                }else
                    Toast.makeText(v.getContext(),"더 이상 늘릴 수 없습니다.",Toast.LENGTH_SHORT).show();
            }
        });
        //요리 방법 삭제 버튼
        btnRemoveRecipe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popRecipe();
            }
        });

        //등록 버튼
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버에 등록데이터 보내고 서버에서 받은 메시지로 결과 출력
                //getServerData(sendRegisterDataToServer());
                String result = "2";
                try {
                    result = sendRegisterDataToServer();
                } catch (JSONException e) { e.printStackTrace();
                } catch (ExecutionException e) { e.printStackTrace();
                } catch (InterruptedException | IOException e) { e.printStackTrace();
                } catch (Exception e) {}
                if(result.equals("1"))
                {
                    Toast.makeText(v.getContext(),"등록 성공",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MyRecipeListView.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    getApplicationContext().startActivity(intent);
                }else
                {
                    Toast.makeText(v.getContext(),"입력 양식에 맞게 입력해 주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //취소 버튼
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    //재료 값 있으면 트루
    private boolean ingredientIsEmpty()
    {
        boolean result = false;
        for(int i =0 ; i < etIngredientList.size(); i++)
        {
            if(etIngredientList.get(i).getText().toString().matches(""))
                result = true;
        }
        return result;
    }
    //요리 방법 있는 지 없는지
    private boolean recipeValueIsEmpty()
    {
        boolean result = false;
        for(int i = 0; i < recipeContentList.size(); i++)
        {
            if(recipeContentList.get(i).getText().toString().matches(""))
                result = true;
        }
        return result;
    }
    ////////////////////////////////////////////////////////////////////////////////
    private void popIngredient()
    {
        if(etIngredientList.size() >=2) {
            etIngredientList.get(etIngredientList.size() - 1).setVisibility(EditText.GONE);
            etIngredientList.remove(etIngredientList.size() - 1);
            etIngredientUnitList.get(etIngredientUnitList.size() - 1).setVisibility(EditText.GONE);
            etIngredientUnitList.remove(etIngredientUnitList.size() - 1);
            spIngredientUnitList.get(spIngredientUnitList.size() - 1).setVisibility(Spinner.GONE);
            spIngredientUnitList.remove(spIngredientUnitList.size() - 1);
        }
    }
    private void popRecipe()
    {
        if(recipeCountList.size() >=2) {
            recipeCountList.get(recipeCountList.size() - 1).setVisibility(View.INVISIBLE);
            recipeCountList.remove(recipeCountList.size() - 1);
            recipeImageViewList.get(recipeImageViewList.size() - 1).setVisibility(ImageView.GONE);
            recipeImageViewList.remove(recipeImageViewList.size() - 1);
            recipeContentList.get(recipeContentList.size() - 1).setVisibility(EditText.GONE);
            recipeContentList.remove(recipeContentList.size() - 1);
            recipeContCnt--;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    public void setDetailImage() // 앨범에서 이미지 가져오기 버튼
    {
        permissionCheck();

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

        recipeDetailImageIdx = recipeImageViewList.size();
    }

    // 레시피 정보 JSON 전송 및 결과 수신
    public String sendRegisterDataToServer() throws JSONException, ExecutionException, InterruptedException, IOException {
        JSONObject recipeJSON = new JSONObject();
        setRecipeData();
        makeRecipeJSON(recipeJSON);

        String result = new RecipeMngAsyncTask().execute("createRecipe", recipeJSON.toString()).get();
        return result;
    }

    // 레시피 JSON 만들기
    public void makeRecipeJSON(JSONObject json) throws JSONException {
        json.accumulate("recipeInId", recipe.getRecipeInId());
        json.accumulate("title", recipe.getTitle());
        json.accumulate("userId", recipe.getUserId());
        json.accumulate("ingredient", recipe.getIngredient());
        json.accumulate("ingredientUnit", recipe.getIngredientUnit());
        json.accumulate("recipePerson", recipe.getRecipePerson());
        json.accumulate("recipeTime", recipe.getRecipeTime());
        json.accumulate("contents", recipe.getContents());

        JSONArray recipeImages = new JSONArray();
        for (int i = 0; i < recipe.getRecipeImageByte().length; i++) {
            char n = (char) (i + '0');
            JSONObject imgJSON = new JSONObject();
            imgJSON.accumulate("recipeImageByte", recipe.getRecipeImageByte()[i]);
            recipeImages.put(imgJSON);
        }
        json.accumulate("recipeImageBytes", recipeImages.toString());
    }

    ////////////////////////////////사용자가 입력한 데이터를 recipe 객체에 set///////////////////////////////
    public void setRecipeData() throws IOException {
        try {
            //유저아이디, 타이틀, 인원, 조리시간

            String strUserId = UserInfo.getString(this, "USER_ID");
            recipe.setUserId(strUserId);
            recipe.setTitle(editTextTitle.getText().toString());
            recipe.setRecipePerson(Integer.parseInt(recipeInfoCount.getText().toString()));
            recipe.setRecipeTime(Integer.parseInt(recipeInfoTime.getText().toString()));

            //대표 이미지, 저장
            //이미지 갯수는 대표 이미지 + 요리 방법에 들어가는 이미지 갯수

            recipe.setRecipeImageByte(getByteArrayFromBitmap(recipeImageViewList, recipeContCnt + 1));

            //재료 데이터
            int i = 0;
            int j = 0;
            for (; i < etIngredientList.size(); i++, j++) {
                ingredient += etIngredientList.get(i).getText().toString();
                ingredientUnit += etIngredientUnitList.get(i).getText().toString();
                ingredientUnit += spIngredientUnitList.get(i).getSelectedItem().toString();


                if (i != etIngredientList.size() - 1) {
                    ingredient += "`";
                    ingredientUnit += "`";
                }
            }
            recipe.setIngredient(ingredient);
            recipe.setIngredientUnit(ingredientUnit);

            //요리 방법 데이터
            String recipeContents = "";
            for (int contCnt = 0; contCnt < recipeContentList.size(); contCnt++) {
                recipeContents += recipeContentList.get(contCnt).getText().toString();
                if (contCnt != recipeContentList.size() - 1) {
                    recipeContents += "`";
                }
            }
            recipe.setContents(recipeContents);

        }catch (Exception e)
        {
            Toast.makeText(this.getApplicationContext(),"내용을 모두 입력해주세요.",Toast.LENGTH_SHORT).show();
        }
    }

    //모든 이미지 데이터를 가져온다.
    public String[] getByteArrayFromBitmap(List<ImageView> imageViewList, int count)
    {
        String[] data = new String[count];
        System.out.println("count: " + count);

        for(int idx = 0; idx < count; idx++) {
            //비트맵으로 바꾼다
            BitmapDrawable drawable = (BitmapDrawable) imageViewList.get(idx).getDrawable();
            Bitmap bitmapImage = drawable.getBitmap();
            //바이트로 바꾸는 작업
            System.out.println(idx + " : " + imageViewList.get(idx).getWidth());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapImage = resizeBitmapImg(bitmapImage,150);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, stream);

            String streamData = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
            data[idx] = streamData;
        }

        return data;
    }

    public Bitmap resizeBitmapImg(Bitmap source, int maxResolution){
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if(width > height){
            if(maxResolution < width){
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        }else{
            if(maxResolution < height){
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
    ///////////////////////////////////////////////////////////////////////////////
    public void permissionCheck()
    {
        //동의 후 실행
        //동적퍼미션 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int permissionResult= checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permissionResult== PackageManager.PERMISSION_DENIED){
                String[] permissions= new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,10);                                     //onRequestPermissionsResult
            }
        }else { return;}
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 10 :
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED) //사용자가 허가 했다면
                {
                    Toast.makeText(this, "외부 메모리 읽기/쓰기 사용 가능", Toast.LENGTH_SHORT).show();

                }else{//거부했다면
                    Toast.makeText(this, "외부 메모리 읽기/쓰기 제한", Toast.LENGTH_SHORT).show();
                    permissionCheck();
                }
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    repPhotoBitmap = BitmapFactory.decodeStream(in);

                    in.close();

                    recipeDetailImage.setImageBitmap(repPhotoBitmap); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    repPhotoBitmap = BitmapFactory.decodeStream(in);
                    in.close();

                    // 요리 정보 이미지 표시
                    System.out.println("imageIndex: " + imageIndex);

                    recipeImageViewList.get(imageIndex).setImageBitmap(repPhotoBitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setIngredient()
    {
        LinearLayout ingredLayout = new LinearLayout(RecipeDetailCreateView.this);
        ingredLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredLayout.setPadding(30,30,30,30);

        EditText etIngredient = new EditText(RecipeDetailCreateView.this);
        etIngredient.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT,2f));
        etIngredient.setPadding(20,20,20,20);
        etIngredient.setHint("재료명");

        EditText etIngredientUnit = new EditText(RecipeDetailCreateView.this);
        etIngredientUnit.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        etIngredientUnit.setPadding(20,20,20,20);
        etIngredientUnit.setHint("재료 양");
        etIngredientUnit.setInputType(0x00000002);           //숫자만

        Spinner spIngredientUnit = new Spinner(RecipeDetailCreateView.this);
        spIngredientUnit.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        spIngredientUnit.setPadding(20,20,20,20);
        //단위 추가
        String[] units = getResources().getStringArray(R.array.unit);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, units);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spIngredientUnit.setAdapter(adapter);

        etIngredientList.add(etIngredient);                         //생성된 재료 editText 를 넣는다.
        etIngredientUnitList.add(etIngredientUnit);                 //생성된 재료단위 editText 를 넣는다.
        spIngredientUnitList.add(spIngredientUnit);

        ingredLayout.addView(etIngredient);
        ingredLayout.addView(etIngredientUnit);
        ingredLayout.addView(spIngredientUnit);
        recipeIngredientLayout.addView(ingredLayout);
    }

    public void setRecipeContent()
    {
        LinearLayout recipeLayout = new LinearLayout(RecipeDetailCreateView.this);
        recipeLayout.setOrientation((LinearLayout.HORIZONTAL));
        recipeLayout.setPadding(30,30,30,30);

        TextView tvRecipeCount = new TextView(this);
        tvRecipeCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        tvRecipeCount.setText(Integer.toString(recipeContCnt + 1));
        recipeContCnt++;

        final ImageView recipeImageView = new ImageView(this);
        // 요리 방법 이미지
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(300, 300,2f);
        recipeImageView.setLayoutParams(paramsImg);
        recipeImageView.setPadding(30,0,30,0);
        recipeImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        recipeImageView.setImageResource(R.drawable.logo);
        final int finalI = recipeContCnt;
        recipeImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 앨범 호출
                Intent intent = new Intent();
                intent.setType("image/*");
                imageIndex = finalI;
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 2);
                System.out.println("온클릿 호출 imageIndex: " +imageIndex);
            }
        });

        EditText etRecipe = new EditText(this);
        etRecipe.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT,6f));
        //etRecipe.setPadding(20,20,20,20);
        etRecipe.setHint("설명");

        recipeCountList.add(tvRecipeCount);
        recipeImageViewList.add(recipeImageView);
        recipeContentList.add(etRecipe);
        recipeLayout.addView(tvRecipeCount);
        recipeLayout.addView(recipeImageView);
        recipeLayout.addView(etRecipe);
        recipeContentLayout.addView(recipeLayout);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void storeCropImage(Bitmap bitmap, String filePath) {                       //자른 이미지를 저장
        try {
            // Recipe 폴더를 생성하여 이미지를 저장하는 방식이다.
            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recipe";

            File directory_Recipe = new File(dirPath);
            if(!directory_Recipe.exists()) // Recipe 디렉터리에 폴더가 없다면 만든다(새로 이미지를 저장할 경우에 속한다.)
                directory_Recipe.mkdir();

            File copyFile = new File(filePath);
            BufferedOutputStream out = null;

            copyFile.createNewFile();

            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(copyFile)));

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

