package io.offscale.frontend_android_auth_scaffold.utils;

import java.io.File;
import java.io.IOError;

import okhttp3.Cache;
import okhttp3.OkHttpClient;


public final class CachedReq {
    private final OkHttpClient.Builder mBuilder;
    private final File mCacheDir;

    CachedReq(final String cacheDir, final String cacheName) {
        mCacheDir = new File(cacheDir, cacheName);
        if (!mCacheDir.isDirectory()) try {
            if (!mCacheDir.mkdirs())
                throw new IOError(new Throwable("Unable to create dir: " + mCacheDir));
        } catch (NullPointerException e) {
            throw new IOError(e);
        }

        mBuilder = new OkHttpClient.Builder()
                .cache(new Cache(
                        mCacheDir,
                        10 * 1024 * 1024 /* 10 MiB*/));
    }

    public final File getCacheDir() {
        return mCacheDir;
    }

    final OkHttpClient.Builder getClientBuilder() {
        return mBuilder;
    }
}
