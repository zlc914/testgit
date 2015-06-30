/*
 * Copyright (c) 2013 NEC Corporation. All rights reserved.
 */
package jp.waseda.nice.notice.object;

import java.io.Serializable;

/**
 * 学年フォムクラス.
 * @author wang hongpeng(NEC)
 */
public class GradeItemForm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3436406438995421275L;
    /**キー.*/
    private String key;
    /**値.*/
    private String value;

    /**
     * キーを返答する.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * キーを設定する.
     *
     * @param property セットする key
     */
    public void setKey(final String property) {
        this.key = property;
    }

    /**
     * 値を返答する.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * 値を設定する.
     *
     * @param property セットする value
     */
    public void setValue(final String property) {
        this.value = property;
    }

}
