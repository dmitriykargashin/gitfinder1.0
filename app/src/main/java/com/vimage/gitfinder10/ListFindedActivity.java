package com.vimage.gitfinder10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ListFindedActivity extends Activity {

    public ListView listviewSearchResult;
    public Context context;
    public String firstURL; // первый адрес для поиска (что ищем)
    public String nextURL; // если есть страницы в поиске (результат более 30), то тут вдрес следующией страницы
    public ParseTask taskToFind; // задача, которая вы выполняется асинхронно и осуществляет поиск
    String stringToFind; // строка поиска

    ArrayList<Repository> repositories; //тут храним список найденных репозиториев
    RepoAdapter repoAdapter; // адаптер для отображения элемнтов репозиория в листвью

    int findedTotalCount; // сколько репозиториев всего нашли поиском
    ContainerBundle bundle; // контейнер для сохранения нужных для восстановления объектов;

    @Override
    public Object onRetainNonConfigurationInstance() {
        // сохраянем наш контейнер для последующего восстановления
        super.onRetainNonConfigurationInstance();
        taskToFind.unBind(); // отвяжем старую активити
        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        context = this; // нужно передевать дальше

        // нашли наш список в аквтивити
        listviewSearchResult = (ListView) findViewById(R.id.listview);

        //нашли строку поиска
        final EditText etSarchedText;
        etSarchedText = (EditText) findViewById(R.id.searchText);

        // попытались восстановить контейнер, если пусто, то создадим список репозиториев
        bundle = (ContainerBundle) getLastNonConfigurationInstance();

        if (bundle != null) { // если было что восстановить, то восстановим
            repositories = bundle.repositories;
            findedTotalCount = bundle.totalCount;
            nextURL = bundle.nextURL;

            if (bundle.parseTask != null) { // если задача поиска запущена
                taskToFind = bundle.parseTask;
                taskToFind.bind(this); // то приявяжем текущую активити
            }


        } else {
            repositories = new ArrayList<>();
        }


        repoAdapter = new RepoAdapter(context, repositories);
        listviewSearchResult.setAdapter(repoAdapter);

        // определим слушателя на скролл
        listviewSearchResult.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount < findedTotalCount) { //если дошли до конца

                    if (taskToFind == null || taskToFind.getStatus() != AsyncTask.Status.RUNNING) // отсечем повторные запросы. если задание запущено, то не стартуем
                    {
                        taskToFind = new ParseTask();
                        taskToFind.bind((ListFindedActivity) context); //  приявяжем текущую активити
                        taskToFind.execute(nextURL);

                    }


                }
            }
        });


        //обработчик нажатия кнопки ввода на строке поиска
        etSarchedText.setOnKeyListener(new View.OnKeyListener() {
                                           public boolean onKey(View v, int keyCode, KeyEvent event) {
                                               if (event.getAction() == KeyEvent.ACTION_DOWN &&
                                                       (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                   // сохраняем текст, введенный до нажатия Enter в переменную
                                                   stringToFind = etSarchedText.getText().toString();

                                                   repositories.clear();// почистим список перед поиском

                                                   // укажем первый адрес откуда начнём поиск, ели нужно, то следующие получаем на пролистывание списка
                                                   firstURL = "https://api.github.com/search/repositories?q=" + stringToFind + "+in:name&sort=stars&order=desc";

                                                   // задание по поиску запускаем без всяких проверок
                                                   taskToFind = new ParseTask();
                                                   taskToFind.bind((ListFindedActivity) context); //  приявяжем текущую активити
                                                   taskToFind.execute(firstURL);
                                                   return true;
                                               }
                                               return false;
                                           }
                                       }
        );


    }

    public void ShowSnackBar(String message) {
        // отбражение всплывающей подсказки
        Snackbar snackbar = Snackbar.make(findViewById(R.id.searchText), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);

        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE); // сделаем цвет текста белым
        snackbar.show();


    }

    static public class ParseTask extends AsyncTask<String, Void, String> {
// класс задания поиска

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String nextFindedURL;
        ListFindedActivity activity;

        // привязываем ссылку на Activity
        void bind(ListFindedActivity bActivity) {
            activity = bActivity;
        }

        // отвязываем
        void unBind() {
            activity = null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.ShowSnackBar("Поиск");
        }

        @Override
        protected String doInBackground(String... params) {
            String link = null;
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(params[0]);// первый параметр это адрес, откуда будем получать ответ

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                link = urlConnection.getHeaderField("Link"); // если есть еще неполученные страницы, то тут будет информация о них

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line); // читаем ответ от сервера
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            nextFindedURL = findNextURL(link);// найдём следующий адрес

            return resultJson; //вернём ответ от сервера
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

            try {
                // создаём объект для нового поиска
                dataJsonObj = new JSONObject(strJson);

                activity.findedTotalCount = dataJsonObj.getInt("total_count"); // количесвто найденных репозиториев

                JSONArray repos = dataJsonObj.getJSONArray("items"); // массив репозиториев из ответа от севера

                for (int i = 0; i < repos.length(); i++) { // для каждого репозитория получим по нему нужные данные
                    JSONObject repo = repos.getJSONObject(i);

                    String name = repo.getString("name"); //имя
                    String url = repo.getString("html_url");// его адрес
                    Integer stars = repo.getInt("stargazers_count"); // количество звёзд

                    //тут добавляем элемент найденное хранилище в список хранилищ
                    activity.repositories.add(new Repository(name, stars, url));
                }

                activity.nextURL = nextFindedURL;// запомним последний следующий адрес;

                // данные обновились, попросим адаптер обновиться
                activity.repoAdapter.notifyDataSetChanged();

                // тут сохраним наши нужные объекты
                activity.bundle = new ContainerBundle(activity.repositories, activity.findedTotalCount, activity.nextURL, this);

                activity.ShowSnackBar("Найдено всего: " + activity.findedTotalCount);
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
