package com.vimage.gitfinder10;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
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

        Repository p = getRepo(position);

        // заполняем View в пункте списка данными
        ((TextView) view.findViewById(R.id.tvName)).setText(p.name);
        ((TextView) view.findViewById(R.id.tvURL)).setText(p.url + "");
        //((ImageView) view.findViewById(R.id.ivStars)).setImageResource(p.image);

        //CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        // присваиваем чекбоксу обработчик
        //cbBuy.setOnCheckedChangeListener(myCheckChangList);
        // пишем позицию
        //cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        //cbBuy.setChecked(p.box);
        return view;
    }

    // товар по позиции
    Repository getRepo(int position) {
        return ((Repository) getItem(position));
    }

    // содержимое корзины
   /* ArrayList<Repository> getBox() {
        ArrayList<Repository> box = new ArrayList<Repository>();
        for (Repository p : objects) {
            // если в корзине
            if (p.box)
                box.add(p);
        }
        return box;
    }
    */

    /*
    // обработчик для чекбоксов
    OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // меняем данные товара (в корзине или нет)
            getProduct((Integer) buttonView.getTag()).box = isChecked;
        }
    };

    */
}

