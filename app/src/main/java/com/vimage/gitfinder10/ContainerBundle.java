package com.vimage.gitfinder10;

import java.util.ArrayList;

/**
 * Created by Dimon on 06.01.2016.
 * Класс контейнер для сохранения необходимых данных при пеересоздании активити
 */
public class ContainerBundle {

    public ArrayList<Repository> repositories;
    public int totalCount;
    public String nextURL;


    public ContainerBundle(ArrayList<Repository> bRepositories, int bTotalCount, String bNextURL) {
        repositories = bRepositories;
        totalCount = bTotalCount;
        nextURL = bNextURL;
    }
}
