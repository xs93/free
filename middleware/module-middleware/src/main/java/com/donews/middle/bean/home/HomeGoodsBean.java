package com.donews.middle.bean.home;

import com.donews.common.contract.BaseCustomViewModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HomeGoodsBean extends BaseCustomViewModel {


    @SerializedName("list")
    private List<GoodsInfo> list;

    public List<GoodsInfo> getList() {
        return list;
    }

    public void setList(List<GoodsInfo> list) {
        this.list = list;
    }

    public static class GoodsInfo extends BaseCustomViewModel{
        @SerializedName("goods_id")
        private String goodsId;
        @SerializedName("title")
        private String title;
        @SerializedName("sales")
        private Integer sales;
        @SerializedName("original_price")
        private float originalPrice;
        @SerializedName("actual_price")
        private float actualPrice;
        @SerializedName("coupon_price")
        private float couponPrice;
        @SerializedName("src")
        private int src;
        @SerializedName("shop_name")
        private String shopName;
        @SerializedName("shop_type")
        private Integer shopType;
        @SerializedName("main_pic")
        private String mainPic;
        @SerializedName("search_id")
        private String searchId;
        @SerializedName("material_id")
        private String materialId;

        public String getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getSales() {
            return sales;
        }

        public void setSales(Integer sales) {
            this.sales = sales;
        }

        public float getOriginalPrice() {
            return originalPrice;
        }

        public void setOriginalPrice(float originalPrice) {
            this.originalPrice = originalPrice;
        }

        public float getActualPrice() {
            return actualPrice;
        }

        public void setActualPrice(float actualPrice) {
            this.actualPrice = actualPrice;
        }

        public float getCouponPrice() {
            return couponPrice;
        }

        public void setCouponPrice(float couponPrice) {
            this.couponPrice = couponPrice;
        }

        public int getSrc() {
            return src;
        }

        public void setSrc(int src) {
            this.src = src;
        }

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public Integer getShopType() {
            return shopType;
        }

        public void setShopType(Integer shopType) {
            this.shopType = shopType;
        }

        public String getMainPic() {
            return mainPic;
        }

        public void setMainPic(String mainPic) {
            this.mainPic = mainPic;
        }

        public String getSearchId() {
            return searchId;
        }

        public void setSearchId(String searchId) {
            this.searchId = searchId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public void setMaterialId(String materialId) {
            this.materialId = materialId;
        }
    }
}
