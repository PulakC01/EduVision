package com.teaminversion.envisionbuddy;

import java.util.List;

public class ResultsItem{
	private String embedUrl;
	private Archives archives;
	private String publishedAt;
	private boolean isAgeRestricted;
	private String description;
	private int likeCount;
	private String viewerUrl;
	private String uri;
	private String staffpickedAt;
	private int faceCount;
	private int vertexCount;
	private int commentCount;
	private List<TagsItem> tags;
	private String uid;
	private String createdAt;
	private Object license;
	private boolean isDownloadable;
	private String name;
	private int animationCount;
	private int viewCount;
	private List<CategoriesItem> categories;
	private Thumbnails thumbnails;
	private User user;

	public String getEmbedUrl(){
		return embedUrl;
	}

	public Archives getArchives(){
		return archives;
	}

	public String getPublishedAt(){
		return publishedAt;
	}

	public boolean isIsAgeRestricted(){
		return isAgeRestricted;
	}

	public String getDescription(){
		return description;
	}

	public int getLikeCount(){
		return likeCount;
	}

	public String getViewerUrl(){
		return viewerUrl;
	}

	public String getUri(){
		return uri;
	}

	public String getStaffpickedAt(){
		return staffpickedAt;
	}

	public int getFaceCount(){
		return faceCount;
	}

	public int getVertexCount(){
		return vertexCount;
	}

	public int getCommentCount(){
		return commentCount;
	}

	public List<TagsItem> getTags(){
		return tags;
	}

	public String getUid(){
		return uid;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public Object getLicense(){
		return license;
	}

	public boolean isIsDownloadable(){
		return isDownloadable;
	}

	public String getName(){
		return name;
	}

	public int getAnimationCount(){
		return animationCount;
	}

	public int getViewCount(){
		return viewCount;
	}

	public List<CategoriesItem> getCategories(){
		return categories;
	}

	public Thumbnails getThumbnails(){
		return thumbnails;
	}

	public User getUser(){
		return user;
	}
}