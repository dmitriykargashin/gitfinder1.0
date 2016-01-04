package com.vimage.gitfinder10;

/**
 * Created by Dimon on 03.01.2016.
 * Класс описывающий объект Репозиторий, будем хранить данные по каждому хранилищу в таком виде
 */


public class Repository {

    String name;
    int stars;
    String url;


    Repository(String _name, int _stars, String _url) {
        name = _name;
        stars = _stars;
        url = _url;
    }
}
