package com.vimage.gitfinder10;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by Dimon on 03.01.2016.
 * Тут описан адаптер для отображения элемента списка репозитория
 */
public class RepoAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Repository> objects;

    RepoAdapter(Context context, ArrayList<Repository> repositories) {
        ctx = context;
        objects = repositories;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view для скорости
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item, parent, false);
        }

        Repository r = getRepo(position);

        // заполняем View в пункте списка данными
        ((TextView) view.findViewById(R.id.tvName)).setText(position + 1 + ". " + r.name);
        ((TextView) view.findViewById(R.id.tvURL)).setText(r.url + "");
        ((TextView) view.findViewById(R.id.tvStars)).setText(r.stars + "");

        return view;
    }


    Repository getRepo(int position) {
        return ((Repository) getItem(position));
    }

}

