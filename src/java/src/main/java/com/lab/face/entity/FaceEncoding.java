package com.lab.face.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FaceEncoding {
    private List<Double> encoding;
    private int top;
    private int left;
    private int right;
    private int bottom;

    public List<Double> getEncoding() {
        return encoding;
    }

    public void setEncoding(List<Double> encoding) {
        this.encoding = encoding;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
}