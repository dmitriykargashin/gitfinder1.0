package com.vimage.gitfinder10;

/**
 * Created by Dimon on 03.01.2016.
 * Класс описывающий объект Репозиторий, будем хранить данные по каждому хранилищу в таком виде
 */


public class Repository {

    String name;
    float stars;
    int image;
    String url;


    Repository(String _name, float _stars, int _image, String _url) {
        name = _name;
        stars = _stars;
        image = _image;
        url = _url;
    }
}
