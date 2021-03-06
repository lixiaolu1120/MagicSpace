package storm.magicspace.http;

import storm.commonlib.common.http.RequestTypes;
import storm.commonlib.common.http.ServiceUtils;
import storm.commonlib.common.http.baseHttpBean.BaseResponse;
import storm.magicspace.bean.httpBean.CheckUpdateResponse;
import storm.magicspace.bean.httpBean.CirclePicResponse;
import storm.magicspace.bean.httpBean.EggImageListResponse;
import storm.magicspace.bean.httpBean.IssueUCGContentResponse;
import storm.magicspace.bean.httpBean.MyCollectionResponse;
import storm.magicspace.bean.httpBean.MyWorksResponse;
import storm.magicspace.bean.httpBean.SubmitUGCContentResponse;
import storm.magicspace.bean.httpBean.SyncAccountResponse;
import storm.magicspace.bean.httpBean.UpdateUGCContentScenesResponse;
import storm.magicspace.bean.httpBean.UserInfoResponse;
import storm.magicspace.bean.httpBean.gameEnd;
import storm.magicspace.http.reponse.AddCollectResponse;
import storm.magicspace.http.reponse.AlbumResponse;
import storm.magicspace.http.reponse.ConponResponse;
import storm.magicspace.http.reponse.EggHttpResponse;
import storm.magicspace.http.reponse.InitResponse;
import storm.magicspace.http.reponse.ShareUrlResponse;
import storm.magicspace.util.LocalSPUtil;

import static storm.commonlib.common.util.StringUtil.EMPTY;

public class HTTPManager {

    public static AlbumResponse test(String contentListType, int page) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_MATERIAL_LIST,
                EMPTY,
                AlbumResponse.class,
                "contentListType", contentListType,
                "page", page
        );
    }

    /**
     * 获取账户信息
     *
     * @return AccountInfoResponse
     */
    public static UserInfoResponse getAccountInfo() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_ACCOUNT_INFO,
                EMPTY,
                UserInfoResponse.class,
                "userId", "1"
        );
    }

    public static InitResponse initAppConfig() {
        return ServiceUtils.request(
                RequestTypes.POST,
                EMPTY,
                EMPTY,
                InitResponse.class
        );
    }

    /**
     * 获取我的作品
     *
     * @return
     */
    public static MyWorksResponse getMyWorks(int page) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_MY_WORKS,
                EMPTY,
                MyWorksResponse.class,
                "page", page,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "authorId", LocalSPUtil.getAccountInfo().getUser_no()
        );
    }

    /**
     * 获取我的收藏
     *
     * @return
     */
    public static MyCollectionResponse getMyCollection(String type, int page) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_MY_COLLECTION,
                EMPTY,
                MyCollectionResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "contentTypeId", type,
                "page", page,
                "pageSize", 20
        );
    }

    /**
     * 获取彩蛋图片列表
     */
    public static EggImageListResponse getEggImageList() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_EGG_IMAGE_LIST,
                EMPTY,
                EggImageListResponse.class
        );

    }

    /**
     * 发表UGC主题
     */
    public static IssueUCGContentResponse issueUCCContent(String description,
                                                          String url,
                                                          String sourceId) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_ISSUE_UGC_CONTENT,
                EMPTY,
                IssueUCGContentResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "description", description,
                "url", url,
                "sourceId", sourceId
        );
    }

    /**
     * 发表UGC主题
     */
    public static IssueUCGContentResponse issueUCCContent(String description,
                                                          String url,
                                                          String sourceId,
                                                          String contentTypeId) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_ISSUE_UGC_CONTENT,
                EMPTY,
                IssueUCGContentResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "description", description,
                "url", url,
                "sourceId", sourceId,
                "contentTypeId", contentTypeId
        );
    }

    /**
     * 更新UGC主题
     */
    public static UpdateUGCContentScenesResponse updateUGCContentScenes(String contendId,
                                                                        String data) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_UPDATE_UGC_CONTENT_SCENES,
                EMPTY,
                UpdateUGCContentScenesResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "contentId", contendId,
                "data", data
        );
    }

    /**
     * 发布游戏
     */
    public static SubmitUGCContentResponse submitUGCContent(String contendId, String data) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_SUBMIT_UGC_CONTENT,
                EMPTY,
                SubmitUGCContentResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "contentId", contendId,
                "data", data
        );
    }

    public static SyncAccountResponse syncAccount(String userId, String data) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_SYNC_ACCOUNT,
                EMPTY,
                SyncAccountResponse.class,
                "userId", userId,
                "data", data
        );
    }


    public static CirclePicResponse getAlbumCirclePic() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_FOCUS_CONTENT_LIST,
                EMPTY,
                CirclePicResponse.class,
                "", ""
        );
    }

    public static ConponResponse GetcouponList() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_COUPON_LIST,
                EMPTY,
                ConponResponse.class,
                "", ""
        );
    }

    public static EggHttpResponse getEggList(int page) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_GET_EGG_LIST,
                EMPTY,
                EggHttpResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "page", page,
                "pageSize", "20"
        );
    }

    public static EggHttpResponse addReport() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.ADDREPORT,
                EMPTY,
                EggHttpResponse.class,
                "userId", "0",
                "versionCode", "1",
                "content", "1",
                "contact", "1"
        );
    }

    public static BaseResponse AutoLogin() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_AUTO_LOGIN,
                EMPTY,
                EggHttpResponse.class,
                "token", LocalSPUtil.getToken(),
                "userId", LocalSPUtil.getLoginAccountId());
    }

    public static CheckUpdateResponse checkAppUpdate() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.CHECK_APP_UPDATE,
                EMPTY,
                CheckUpdateResponse.class);
    }

    public static AddCollectResponse addCollect(String contentId, String type) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.URL_ADD_COLLECTION,
                EMPTY,
                AddCollectResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "contentId", contentId,
                "contentTypeId", type.toLowerCase()
        );
    }

    public static ShareUrlResponse getUrl() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.GETAPPSHARELINK,
                EMPTY,
                ShareUrlResponse.class
        );
    }

    public static ShareUrlResponse gameEnd(gameEnd mgameEnd) {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.GAMEEND,
                EMPTY,
                ShareUrlResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no(),
                "contentId", mgameEnd.contentId,
                "duration", mgameEnd.duration,
                "isWon", mgameEnd.isWon
        );
    }

    public static ShareUrlResponse reqInfoCallback() {
        return ServiceUtils.request(
                RequestTypes.POST,
                URLConstant.REQINFOCALLBACK,
                EMPTY,
                ShareUrlResponse.class,
                "userId", LocalSPUtil.getAccountInfo().getUser_no()
        );
    }
}