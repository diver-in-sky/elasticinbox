package com.elasticinbox.pipe.elasticsearch;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class IndexedMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> from;

    private List<String> to;

    private List<String> cc;

    private List<String> bcc;

    private String subject;

    private Date date;

    private long size;

    private String plainBody;

    private List<Integer> labels;

    private List<Integer> markers;

    private String userId;

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPlainBody() {
        return plainBody;
    }

    public void setPlainBody(String plainBody) {
        this.plainBody = plainBody;
    }

    public List<Integer> getLabels() {
        return labels;
    }

    public void setLabels(List<Integer> labels) {
        this.labels = labels;
    }

    public List<Integer> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Integer> markers) {
        this.markers = markers;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
