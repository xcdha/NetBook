package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChapterBuffer {
    private static final String TAG = "ChapterBuffer";
    private byte[] mBuffer;
    private long mBufferLen;
    private final String mBookId;
    private final int mChapter;
    private final List<PageLines> mPageList = new ArrayList<>();
    private String mCharset = "UTF-8";
    private int mReadPos = 0;

    public ChapterBuffer(String bookId, int chapter) {
        mBookId = bookId;
        mChapter = chapter;
    }

    public boolean openCacheBookChapter() {
        boolean success = false;
        File file = FileUtils.getChapterFile(mBookId, mChapter);
        if (file.exists() && file.length() > 10) {
            mCharset = FileUtils.getCharset(file.getAbsolutePath());
            RandomAccessFile raf = null;
            try {
                mBufferLen = file.length();
                mBuffer = new byte[(int) mBufferLen];
                raf = new RandomAccessFile(file, "r");
                int i = raf.read(mBuffer);
                Log.i(TAG, "openCacheBookChapter:mBufferLen = " + i);
                if (i == -1 || i == mBufferLen) {
                    success = true;
                }
            } catch (Exception e) {
                Log.e(TAG, e);
                success = false;
            } finally {
                AppUtils.close(raf);
            }
        }
        return success;
    }

    public boolean openNetBookChapter(Entities.Chapter data, boolean hasSave) {
        mCharset = "UTF-8";
        String body = AppUtils.formatContent(data.body);
        if (hasSave) {
            File file = FileUtils.getChapterFile(mBookId, mChapter);
            FileUtils.writeFile(file.getAbsolutePath(), body, false);
        }
        try {
            mBuffer = body.getBytes(mCharset);
            mBufferLen = mBuffer.length;
//            Log.i(TAG, "openNetBookChapter:mBufferLen = " + mBufferLen);
            return true;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e);
        }
        return false;
    }

    /**
     * 从pos开始读取一个段落
     */
    private byte[] readParagraphForward(int pos) {
        byte b0;
        int i = pos;
        while (i < mBufferLen) {
            b0 = mBuffer[i++];
            if (b0 == 0x0a) {
                break;
            }
        }
        int size = i - pos;
        byte[] buf = new byte[size];
        for (i = 0; i < size; i++) {
            buf[i] = mBuffer[pos + i];
        }
        return buf;
    }

    /**
     * 计算一共有多少页，保存每一页的数据
     *
     * @param maxLineCount 一页最大行数
     * @param paint        TextView Paint
     * @param width        TextView width
     */
    public void calcPageLines(int maxLineCount, Paint paint, int width) {
        mReadPos = 0;
        mPageList.clear();
        int pageNumber = 0;
        while (mReadPos < mBufferLen) {
            mPageList.add(calcOnePage(maxLineCount, paint, width, pageNumber));
            pageNumber++;
        }
    }

    private PageLines calcOnePage(int maxLineCount, Paint paint, int width, int pageNumber) {
        String paragraphStr = "";
        PageLines pageContent = new PageLines();
        pageContent.lines = new ArrayList<>();
        pageContent.startPos = mReadPos;
        pageContent.page = pageNumber;
        while (pageContent.lines.size() < maxLineCount && mReadPos < mBufferLen) {
            byte[] paragraph = readParagraphForward(mReadPos);
            mReadPos += paragraph.length;
            try {
                paragraphStr = new String(paragraph, mCharset);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e);
            }
            while (paragraphStr.length() > 0) {
                int paintSize = paint.breakText(paragraphStr, true, width - 15, null);
                String line = paragraphStr.substring(0, paintSize);
                if (AppUtils.isEmpty(line.trim())) {
                    paragraphStr = paragraphStr.substring(paintSize);
                    continue;
                }
                if (line.length() > 5 && line.charAt(0) == ' ' && line.charAt(1) == ' '
                        && line.charAt(2) == ' ' && line.charAt(3) == ' ') {
                    line = line.substring(2);
                }
                pageContent.lines.add(line);
                paragraphStr = paragraphStr.substring(paintSize);
                if (pageContent.lines.size() >= maxLineCount) {
                    break;
                }
            }
            if (paragraphStr.length() > 0) {
                try {
                    mReadPos -= (paragraphStr).getBytes(mCharset).length;
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e);
                }
            }
        }
        pageContent.endPos = mReadPos;
        return pageContent;
    }

    public PageLines getPageForReadPos(int readPos) {
        for (PageLines content : mPageList) {
            if (readPos >= content.startPos && readPos < content.endPos) {
                return content;
            }
        }
        if (readPos == -1 && mPageList.size() > 0) {
            return mPageList.get(mPageList.size() - 1);
        }
        return mPageList.get(0);
    }

    public PageLines getPageForPos(int pageNumber) {
        if (pageNumber >= getPageCount() || pageNumber < 0) {
            Log.e(TAG, "getPageForPos " + pageNumber + " error!");
            return mPageList.get(0);
        }
        return mPageList.get(pageNumber);
    }

    public PageLines getEndPage() {
        return mPageList.get(mPageList.size() - 1);
    }

    public int getPageCount() {
        return mPageList.size();
    }
}
