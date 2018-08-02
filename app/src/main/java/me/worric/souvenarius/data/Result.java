package me.worric.souvenarius.data;

/**
 * Inspired by <a href="https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample">Google's GithubBrowser example</a>
 */
public final class Result<T> {

    public final T response;
    public final Status status;
    public final String message;

    private Result(T response, Status status, String message) {
        this.response = response;
        this.status = status;
        this.message = message;
    }

    public static <T> Result<T> success(T response) {
        return new Result<>(response, Status.SUCCESS, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(null, Status.FAILURE, message);
    }

    public enum Status {
        SUCCESS,
        FAILURE
    }

}

