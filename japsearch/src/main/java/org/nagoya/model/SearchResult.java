package org.nagoya.model;


import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;
import org.nagoya.model.dataitem.FxThumb;

public class SearchResult {

	private String urlPath;
	private String label;
	private Option<FxThumb> previewImage = Option.none();
	private boolean isJSONSearchResult;

	public SearchResult(String urlPath, String label, FxThumb previewImage) {
		super();
		this.urlPath = urlPath;
		this.previewImage = Option.of(previewImage);
		this.label = label;
	}

	public SearchResult(String urlPath, String label, Option<FxThumb> previewImage) {
		super();
		this.urlPath = urlPath;
		this.previewImage = previewImage;
		this.label = label;
	}

	public SearchResult(String urlPath) {
		this.urlPath = urlPath;
		previewImage = Option.none();
		label = "";
	}

	public SearchResult(String urlPath, String label) {
		this.urlPath = urlPath;
		this.label = label;
		previewImage = Option.none();
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public Option<FxThumb> getPreviewImage() {
		return previewImage;
	}

	public void setPreviewImage(FxThumb previewImage) {
		this.previewImage = Option.of(previewImage);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		if (label.length() > 0)
			return label + " - " + urlPath;
		else
			return urlPath;
	}

	public boolean isJSONSearchResult() {
		return isJSONSearchResult;
	}

	public void setJSONSearchResult(boolean isJSONSearchResult) {
		this.isJSONSearchResult = isJSONSearchResult;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isJSONSearchResult ? 1231 : 1237);
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((previewImage == null) ? 0 : previewImage.hashCode());
		result = prime * result + ((urlPath == null) ? 0 : urlPath.hashCode());
		return result;
	}

	@Contract(value = "null -> false", pure = true)
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchResult other = (SearchResult) obj;
		if (isJSONSearchResult != other.isJSONSearchResult)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (previewImage == null) {
			if (other.previewImage != null)
				return false;
		} else if (!previewImage.equals(other.previewImage))
			return false;
		if (urlPath == null) {
			return other.urlPath == null;
		} else return urlPath.equals(other.urlPath);
	}

}
