package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.RssSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RssSourceMapper {
    /**
     * 查询所有RSS源
     * @return RSS源列表
     */
    List<RssSource> findAll();

    /**
     * 根据ID查询RSS源
     * @param id RSS源ID
     * @return RSS源
     */
    RssSource findById(Integer id);

    /**
     * 条件查询RSS源
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 状态
     * @return RSS源列表
     */
    List<RssSource> findByFilters(@Param("keyword") String keyword, 
                               @Param("categoryId") Integer categoryId, 
                               @Param("status") String status);

    /**
     * 插入RSS源
     * @param rssSource RSS源
     * @return 影响的行数
     */
    int insert(RssSource rssSource);

    /**
     * 更新RSS源
     * @param rssSource RSS源
     * @return 影响的行数
     */
    int update(RssSource rssSource);

    /**
     * 删除RSS源
     * @param id RSS源ID
     * @return 影响的行数
     */
    int deleteById(Integer id);

    /**
     * 更新RSS源最后抓取时间
     * @param id RSS源ID
     * @return 影响的行数
     */
    @Update("UPDATE rss_source SET last_fetch_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updateLastFetchTime(Integer id);

    /**
     * 统计RSS源总数
     */
    @Select("SELECT COUNT(*) FROM rss_source")
    int countTotal();

    /**
     * 统计不同状态的RSS源数量
     */
    @Select("SELECT COUNT(*) FROM rss_source WHERE status = #{status}")
    int countByStatus(@Param("status") String status);
}
