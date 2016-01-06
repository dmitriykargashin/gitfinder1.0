package com.vimage.gitfinder10;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ScrollingActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";
    public ListView listviewSearchResult;
    public Context context;
    public String firstURL;
    public String nextURL;
    public ParseTask taskToFind;

    ArrayList<Repository> repositories = new ArrayList<Repository>(); //тут храним список найденных репозиториев
    RepoAdapter repoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        context = this;
        // нашли наш список
        listviewSearchResult = (ListView) findViewById(R.id.listview);

        listviewSearchResult.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    taskToFind = new ParseTask();
                    taskToFind.execute(nextURL);
                }
            }
        });




        //нашли строку поиска
        final EditText searchedText;
        searchedText = (EditText) findViewById(R.id.searchText);

        //обработчик нажатия кнопки ввода на строке поиска
        searchedText.setOnKeyListener(new View.OnKeyListener() {
                                          public boolean onKey(View v, int keyCode, KeyEvent event) {
                                              if (event.getAction() == KeyEvent.ACTION_DOWN &&
                                                      (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                  // сохраняем текст, введенный до нажатия Enter в переменную
                                                  String stringToFind = searchedText.getText().toString();

                                                  repositories.clear();// почистим список перед поиском
                                                  repoAdapter = new RepoAdapter(context, repositories);
                                                  listviewSearchResult.setAdapter(repoAdapter);

                                                  // укажем первый адрес откуда начнём поиск
                                                  firstURL = "https://api.github.com/search/repositories?q=" + stringToFind + "+in:name&sort=stars&order=desc";
                                                  taskToFind = new ParseTask();
                                                  taskToFind.execute(firstURL);
                                                  //nextURL = taskToFind.get();

                                                  return true;
                                              }
                                              return false;
                                          }
                                      }
        );

    }

    public void ShowSnackBar(String message) {
        Snackbar.make(findViewById(R.id.searchText), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    private class ParseTask extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String nextFindedURL;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ShowSnackBar("Начали поиск");
        }

        @Override
        protected String doInBackground(String... params) {
            String link = null;
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                link = urlConnection.getHeaderField("Link");

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            nextFindedURL = findNextURL(link);// найдём следующий адрес

            return resultJson;
        }

        private String findNextURL(String link) {
            // тут будем искать строку с next и найдём адрес в этой строке
            String next = "";

            if (link == null) {
                return next;
            }

            //находим "next" в подстроке
            boolean hasNext = link.contains("next");
            //вычленяем адрес из подстроки
            if (hasNext) {
                int pos = link.indexOf(">");
                next = link.substring(1, pos);
            }

            return next;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);


            JSONObject dataJsonObj;
            String totalcount;


            try {
                // создаём объект для нового поиска
                dataJsonObj = new JSONObject(strJson);

                totalcount = dataJsonObj.get("total_count").toString();

                JSONArray repos = dataJsonObj.getJSONArray("items");

                for (int i = 0; i < repos.length(); i++) {
                    JSONObject repo = repos.getJSONObject(i);

                    String name = repo.getString("name");
                    String url = repo.getString("html_url");
                    Integer stars = repo.getInt("stargazers_count");


                    //        Log.d(LOG_TAG, "name: " + name);
                    //      Log.d(LOG_TAG, "url: " + url);

                    //тут добавляем элемент найденное хранилище в список хранилищ
                    repositories.add(new Repository(name, stars, url));
                }


                // repoAdapter = new RepoAdapter(context, repositories);

                nextURL = nextFindedURL;// запомним последний следующий адрес;
                // данные обновились, попросим адаптер обновиться
                repoAdapter.notifyDataSetChanged();
                // настраиваем список
                //listviewSearchResult.setAdapter(repoAdapter);


                ShowSnackBar("Найдено: " + totalcount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
