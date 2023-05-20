package com.vitargo.kpi_journal.model;


import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public class Article {
    private long id;
    private String name;
    private String url;
    private String path;

    private String code;

    public Article() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return "Article = {name = " + this.getName()
                + ", url = " + this.getUrl();
    }
}
