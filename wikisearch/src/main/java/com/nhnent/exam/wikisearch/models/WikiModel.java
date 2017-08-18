package com.nhnent.exam.wikisearch.models;

/**
 * Created by gradler on 18/08/2017.
 */

public class WikiModel {
    private String thumbnailUrl;
    private String displayTitle;
    private String extractText;

    private boolean isHeader;

    public WikiModel() {}

    public WikiModel(String thumbnailUrl, String displayTitle, String extractText) {
        this.thumbnailUrl = thumbnailUrl;
        this.displayTitle = displayTitle;
        this.extractText = extractText;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getExtractText() {
        return extractText;
    }

    public void setExtractText(String extractText) {
        this.extractText = extractText;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }
}
