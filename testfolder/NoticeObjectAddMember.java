/*
 * Copyright (c) 2013 NEC Corporation. All rights reserved.
 */
package jp.waseda.nice.notice.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.waseda.nice.common.CommonConstant;
import jp.waseda.nice.common.CommonConstant.DefaultPropertyKeys;
import jp.waseda.nice.common.SessionKeyConstant;
import jp.waseda.nice.common.pagination.PaginationList;
import jp.waseda.nice.common.user.UserInformation;
import jp.waseda.nice.common.util.CommonUtil;
import jp.waseda.nice.notice.edit.NoticeEditForm;
import jp.waseda.nice.notice.edit.NoticeEditLogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

/**
 * お知らせ通知対象（メンバー追加）画面アクションクラス.
 * @author wang hongpeng(NEC)
 */
@Results({
        @Result(name = "add",
                location = "../edit/notice-edit-edit",
                type = "redirect",
                params = { "communityId", "%{communityId}",
                        "portalCommunityId", "%{portalCommunityId}",
                        "communityContentLinkId", "%{communityContentLinkId}",
                        "contentId", "%{contentId}",
                        "contentVersion", "%{contentVersion}",
                        "corUflag", "%{corUflag}",
                        "linkFlag", "%{linkFlag}",
                        "copyFlag", "%{copyFlag}",
                        "mode", "%{mode}",
                        "backMode", "1" }),
})
@Component
public class NoticeObjectAddMember extends ActionSupport implements SessionAware {

    /**
     * シリアル・バージョンID.
     */
    private static final long serialVersionUID = 1L;
    /** コミュニティID. */
    private String communityId;
    /** ポータルコミュニティID. */
    private String portalCommunityId;
    /** 選択したアカウントリスト. */
    private List<String> accountList;
    /** コミュニティリスト. */
    private List<NoticeObjectListForm> itemList;
    /** ページデータ.*/
    private HashMap<String, Object> paginationResultData;
    /** ページ数.*/
    private Integer paginationTargetPage;
    /** マイページデータ数.*/
    private Integer paginationRowsPerPage;
    /** 増分件数.*/
    private String showMoreCount;
    /** 検索フォム.*/
    private NoticeObjectSearchForm searchForm;
    /** 氏名. */
    private String name;
    /** ソート. */
    private String sortCode;
    /** コンテンツID. */
    private String contentId;
    /** コンテンツバージョン. */
    private String contentVersion;
    /** コミュニティコンテンツID. */
    private String communityContentLinkId;
    /** 新規・更新フラグ. */
    private String corUflag;
    /** コピーフラグ. */
    private String copyFlag;
    /** リンクフラグ. */
    private String linkFlag;
    /** モード. */
    private String mode;
    /** コミュニティ名. */
    private String communityName;
    /** タイトル標識.*/
    private String titleFlag = "";
    /** メンバー用コミュニティID. */
    private String selCommunityId;

    /** セッション.*/
    private Map<String, Object> session;
    /** お知らせ通知対象ロジック. */
    private NoticeObjectLogic logic;
    /** 選択の言語.*/
    private String selectLanguage;
    /** お知ら編集ロジック.*/
    private NoticeEditLogic noticeEditLogic;

    //------------------ 画面遷移制御 ----------------------
    /**
     * お知らせ通知対象（メンバー追加）画面 起動制御.
     *
     * @return 戻り値文字列
     */
    @Override
    public String execute() {
        String result = checkPermission();
        if (null != result) {
            return result;
        }
        this.name = "";
        this.sortCode = "1";
        this.searchForm = new NoticeObjectSearchForm();
        this.searchForm.setName(null);
        this.searchForm.setSortCode("1");
        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);
        this.showMoreCount = this.logic.getDefaultSetting(
                CommonUtil.convertStrToLong(this.communityId),
                DefaultPropertyKeys.NOTICE_OBJECT_ADD_MEMBER_SHOW_COUNT);
        this.communityName = this.logic.getCommunityName(CommonUtil.convertStrToLong(communityId));
        // ヘッダ表示フラグの取得
        if (null != this.session.get(SessionKeyConstant.SESSION_KEY_CALL_SOURCE_DIFF)) {
            this.titleFlag = this.session.get(
                    SessionKeyConstant.SESSION_KEY_CALL_SOURCE_DIFF).toString();
        }
        return SUCCESS;
    }

    /**
     * お知らせ通知対象（メンバー追加）画面で参照ボタンが押下処理.
     * 個人設定画面へ遷移する.
     *
     * @return 戻り値文字列
     */
    public String back() {
        // Sessionの検索条件をクリア
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            session.remove(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
        }
        return "add";
    }

    /**
     * お知らせ通知対象（メンバー追加）画面で検索ボタンが押下処理.
     *
     * @return 戻り値文字列
     */
    public String search() {
        // 検索条件の設定
        this.searchForm = new NoticeObjectSearchForm();
        this.searchForm.setName(this.name);
        String sort = "";
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            NoticeObjectSearchForm sessionForm = new NoticeObjectSearchForm();
            // セッションから読み返し
            sessionForm = (NoticeObjectSearchForm) session.get(
                    SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
            sort = sessionForm.getSortCode();
        }
        searchForm.setSortCode(sort);
        this.setSortCode(sort);

        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);

        return SUCCESS;
    }

    /**
     * お知らせ通知対象（メンバー追加）画面で検索リセットボタンが押下処理.
     *
     * @return 戻り値文字列
     */
    public String searchReset() {
        // 検索条件の設定
        this.searchForm = new NoticeObjectSearchForm();
        this.searchForm.setName(null);
        String sort = "";
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            NoticeObjectSearchForm sessionForm = new NoticeObjectSearchForm();
            // セッションから読み返し
            sessionForm = (NoticeObjectSearchForm) session.get(
                    SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
            sort = sessionForm.getSortCode();
        }
        searchForm.setSortCode(sort);
        this.setSortCode(sort);
        this.name = "";

        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);

        return SUCCESS;
    }

    /**
     * お知らせ通知対象（メンバー追加）画面でソートボタンが押下処理.
     *
     * @return 戻り値文字列
     */
    public String sort() {
        // 検索条件の設定
        this.searchForm = new NoticeObjectSearchForm();
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            // セッションから読み返し
            searchForm = (NoticeObjectSearchForm) session.get(
                    SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
            this.name = searchForm.getName();
        }
        searchForm.setSortCode(this.sortCode);

        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);

        return SUCCESS;
    }

    /**
     * お知らせ通知対象（メンバー追加）画面でソートリセットボタンが押下処理.
     *
     * @return 戻り値文字列
     */
    public String sortReset() {
        // 検索条件の設定
        this.searchForm = new NoticeObjectSearchForm();
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            // セッションから読み返し
            searchForm = (NoticeObjectSearchForm) session.get(
                    SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
            this.name = searchForm.getName();
        }
        searchForm.setSortCode("1");
        this.sortCode = "1";

        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);

        return SUCCESS;
    }

    /**
     * もっと見るボタンを押下する.
     *
     * @return 戻り値文字列
     */
    public String more() {
        String result = checkPermission();
        if (null != result) {
            return result;
        }
        // 検索条件の設定
        this.searchForm = new NoticeObjectSearchForm();
        // sessionから、検索条件の取得
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM)) {
            NoticeObjectSearchForm settingSearchForm = new NoticeObjectSearchForm();
            settingSearchForm = (NoticeObjectSearchForm) session
                    .get(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
            this.searchForm.setName(settingSearchForm.getName());
            this.searchForm.setSortCode(settingSearchForm.getSortCode());
        }

        List<String> selectedCodeList = new ArrayList<String>();
        if (null != session.get(
                SessionKeyConstant.SESSION_KEY_NOTICE_EDIT_EDIT_INPUTFORM)) {
            NoticeEditForm editForm = (NoticeEditForm) session
                    .get(SessionKeyConstant.SESSION_KEY_NOTICE_EDIT_EDIT_INPUTFORM);
            selectedCodeList = this.logic.getAccountIDList(editForm.getAccountList());
        }

        // コミュニティメンバーの取得
        this.itemList = this.logic.getMemberList(CommonUtil.convertStrToLong(selCommunityId),
                searchForm, selectedCodeList);

        // 検索条件をSessionに置く
        this.session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM,
                searchForm);

        PaginationList<NoticeObjectListForm> pageList =
                new PaginationList<NoticeObjectListForm>(
                        this.itemList,
                        paginationTargetPage,
                        paginationRowsPerPage,
                        this.getText("common.showmore"));
        // ページメソッドを呼び出す
        paginationResultData = pageList.pagination();
        // ページ化失敗場合
        if (("1").equals(pageList.getErrorCode())) {
            paginationResultData.put(PaginationList.ERROR_MESSAGE,
                    this.getText("message.empty.list"));
        }
        return "paginationResult";
    }

    /**
     * お知らせ通知対象（メンバー追加）画面で追加ボタンが押下処理.
     *
     * @return 戻り値文字列
     */
    public String add() {
        // Sessionの検索条件をクリア
        session.remove(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_SEARCH_FORM);
        // 選択したメンバーをセッションに設定する
        session.remove(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_COMMUNITY_ADD_MEMBER);
        session.put(SessionKeyConstant.SESSION_KEY_NOTICE_OBJECT_COMMUNITY_ADD_MEMBER,
                this.accountList);
        // 通知対象（コミュニティ）画面に遷移する
        return "add";
    }

    /**
     * 権限チェック.
     * @return 文字列
     */
    private String checkPermission() {
        if (StringUtils.isEmpty(communityContentLinkId)) {
            if (null == session.get(SessionKeyConstant.SESSION_KEY_SELCOMMUNITYPAGEID)) {
                return CommonConstant.PERMISSION;
            } else {
                String communityPageIdStr =
                        session.get(SessionKeyConstant.SESSION_KEY_SELCOMMUNITYPAGEID).toString();
                Long communityPageId = CommonUtil.convertStrToLong(communityPageIdStr);
                boolean permissionFlag =
                        noticeEditLogic.hasCreatePermission(UserInformation.getCurrentPersonId(),
                                CommonUtil.convertStrToLong(communityId),
                                communityPageId);
                if (!permissionFlag) {
                    return CommonConstant.PERMISSION;
                }
            }

        } else {
            boolean permissionFlag =
                    noticeEditLogic.hasEditPermission(UserInformation.getCurrentPersonId(),
                            CommonUtil.convertStrToLong(communityId),
                            CommonUtil.convertStrToLong(communityContentLinkId));
            if (!permissionFlag) {
                return CommonConstant.PERMISSION;
            }
        }
        return null;
    }

    //------------------ getter及びsetter ------------------

    /**
     * コミュニティIDを返答する.
     *
     * @return コミュニティID
     */
    public String getCommunityId() {
        return communityId;
    }

    /**
      * コミュニティIDを設定する.
      *
      * @param property セットする communityId
      */
    public void setCommunityId(final String property) {
        this.communityId = property;
    }

    /**
     * ポータルコミュニティIDを返答する.
     *
     * @return ポータルコミュニティID
     */
    public String getPortalCommunityId() {
        return this.portalCommunityId;
    }

    /**
     * ポータルコミュニティIDを設定する.
     *
     * @param property セットする portalCommunityId
     */
    public void setPortalCommunityId(final String property) {
        this.portalCommunityId = property;
    }

    /**
     * 選択したコミュニティアカウントリストを返答する.
     *
     * @return idList
     */
    public List<String> getAccountList() {
        return accountList;
    }

    /**
     * 選択したコミュニティアカウントリストを設定する.
     *
     * @param property セットする accountList
     */
    public void setAccountList(final List<String> property) {
        this.accountList = property;
    }

    /**
     * コミュニティリストを返答する.
     *
     * @return itemList
     */
    public List<NoticeObjectListForm> getItemList() {
        return itemList;
    }

    /**
     * コミュニティリストを設定する.
     *
     * @param property セットする itemList
     */
    public void setItemList(final List<NoticeObjectListForm> property) {
        this.itemList = property;
    }

    /**
     * ページデータを返答する.
     *
     * @return paginationResultData
     */
    public HashMap<String, Object> getPaginationResultData() {
        return paginationResultData;
    }

    /**
     * ページデータを設定する.
     *
     * @param property セットする paginationResultData
     */
    public void setPaginationResultData(final HashMap<String, Object> property) {
        this.paginationResultData = property;
    }

    /**
     * ページ数を返答する.
     *
     * @return paginationTargetPage
     */
    public Integer getPaginationTargetPage() {
        return paginationTargetPage;
    }

    /**
     * ページ数を設定する.
     *
     * @param property セットする paginationTargetPage
     */
    public void setPaginationTargetPage(final Integer property) {
        this.paginationTargetPage = property;
    }

    /**
     * マイページデータ数を返答する.
     *
     * @return paginationRowsPerPage
     */
    public Integer getPaginationRowsPerPage() {
        return paginationRowsPerPage;
    }

    /**
     * マイページデータ数を設定する.
     *
     * @param property セットする paginationRowsPerPage
     */
    public void setPaginationRowsPerPage(final Integer property) {
        this.paginationRowsPerPage = property;
    }

    /**
     * 増分件数を返答する.
     *
     * @return showMoreCount
     */
    public String getShowMoreCount() {
        return showMoreCount;
    }

    /**
     * 増分件数を設定する.
     *
     * @param property セットする showMoreCount
     */
    public void setShowMoreCount(final String property) {
        this.showMoreCount = property;
    }

    /**
     * 検索フォムを返答する.
     *
     * @return searchForm
     */
    public NoticeObjectSearchForm getSearchForm() {
        return searchForm;
    }

    /**
     * 検索フォムを設定する.
     *
     * @param property セットする searchForm
     */
    public void setSearchForm(final NoticeObjectSearchForm property) {
        this.searchForm = property;
    }

    /**
     * 氏名を返答する.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 氏名を設定する.
     *
     * @param property セットする name
     */
    public void setName(final String property) {
        this.name = property;
    }

    /**
     * ソートを返答する.
     *
     * @return sortCode
     */
    public String getSortCode() {
        return sortCode;
    }

    /**
     * ソートを設定する.
     *
     * @param property セットする sortCode
     */
    public void setSortCode(final String property) {
        this.sortCode = property;
    }

    /**
     * セッションを設定する.
     * @param property セットする セッション
     */
    @Override
    public void setSession(final Map<String, Object> property) {
        this.session = property;
    }

    /**
     * お知らせ通知対象ロジックを設定する.
     * @param property セットする logic.
     */
    @Autowired
    public void setLogic(final NoticeObjectLogic property) {
        this.logic = property;
    }

    /**
     * コンテンツIDを返答する.
     *
     * @return コンテンツID
     */
    public String getContentId() {
        return contentId;
    }

    /**
      * コンテンツIDを設定する.
      *
      * @param property セットする contentId
      */
    public void setContentId(final String property) {
        this.contentId = property;
    }

    /**
     * コミュニティコンテンツIDを返答する.
     *
     * @return コミュニティコンテンツID
     */
    public String getContentVersion() {
        return contentVersion;
    }

    /**
      * コミュニティコンテンツIDを設定する.
      *
      * @param property セットする contentVersion
      */
    public void setContentVersion(final String property) {
        this.contentVersion = property;
    }

    /**
     * コミュニティコンテンツIDを返答する.
     *
     * @return コミュニティコンテンツID
     */
    public String getCommunityContentLinkId() {
        return communityContentLinkId;
    }

    /**
      * コミュニティコンテンツIDを設定する.
      *
      * @param property セットする communityContentLinkId
      */
    public void setCommunityContentLinkId(final String property) {
        this.communityContentLinkId = property;
    }

    /**
     * 新規・更新フラグを返答する.
     *
     * @return 新規・更新フラグ
     */
    public String getCorUflag() {
        return corUflag;
    }

    /**
      * 新規・更新フラグを設定する.
      *
      * @param property セットする corUflag
      */
    public void setCorUflag(final String property) {
        corUflag = property;
    }

    /**
     * コピーフラグを返答する.
     *
     * @return コピーフラグ
     */
    public String getCopyFlag() {
        return copyFlag;
    }

    /**
     * コピーフラグを設定する.
     *
     * @param property
     *            セットする copyFlag
     */
    public void setCopyFlag(final String property) {
        copyFlag = property;
    }

    /**
     * リンクフラグを返答する.
     *
     * @return リンクフラグ
     */
    public String getLinkFlag() {
        return linkFlag;
    }

    /**
      * リンクフラグを設定する.
      *
      * @param property セットする linkFlag
      */
    public void setLinkFlag(final String property) {
        linkFlag = property;
    }

    /**
     * モードを設定する.
     *
     * @param property モード
     */
    public void setMode(final String property) {
        mode = property;
    }

    /**
     * モードを返答する.
     *
     * @return モード
     */
    public String getMode() {
        return mode;
    }

    /**
     * コミュニティ名を返答する.
     *
     * @return communityName
     */
    public String getCommunityName() {
        return communityName;
    }

    /**
     * コミュニティ名を設定する.
     *
     * @param property セットする communityName
     */
    public void setCommunityName(final String property) {
        this.communityName = property;
    }

    /**
     * タイトル標識を返答する.
     *
     * @return titleFlag タイトル標識
     */
    public String getTitleFlag() {
        return this.titleFlag;
    }

    /**
     * タイトル標識を設定する.
     *
     * @param property セットする titleFlag
     */
    public void setTitleFlag(final String property) {
        this.titleFlag = property;
    }

    /**
     * selectLanguageを返答する.
     *
     * @return selectLanguage
     */
    public String getSelectLanguage() {
        return selectLanguage;
    }

    /**
     * selectLanguageを設定する.
     *
     * @param property セットする selectLanguage
     */
    public void setSelectLanguage(final String property) {
        this.selectLanguage = property;
    }

    /**
     * noticeEditLogicを設定する.
     *
     * @param property セットする noticeEditLogic
     */
    @Autowired
    public void setNoticeEditLogic(final NoticeEditLogic property) {
        this.noticeEditLogic = property;
    }

    /**
     * メンバー用コミュニティIDを返答する.
     *
     * @return selCommunityId
     */
    public String getSelCommunityId() {
        return selCommunityId;
    }

    /**
     * メンバー用コミュニティIDを設定する.
     *
     * @param property セットする selCommunityId
     */
    public void setSelCommunityId(final String property) {
        this.selCommunityId = property;
    }
}
