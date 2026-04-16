package com.zzl.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.platform.auth.entity.SysDepartment;
import com.zzl.platform.auth.vo.DepartmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {

    /**
     * 查询所有部门树
     */
    @Select("<script>" +
            "SELECT " +
            "  d.id, d.dept_code, d.dept_name, d.parent_id, d.dept_level, d.dept_path, " +
            "  d.dept_sort, d.leader, d.leader_phone, d.email, d.status, " +
            "  d.remark, d.create_time, d.update_time " +
            "FROM t_sys_department d " +
            "WHERE d.deleted = 0 " +
            "ORDER BY d.parent_id, d.dept_sort " +
            "</script>")
    List<DepartmentVO> selectAllDepartments();

    /**
     * 查询部门的用户数量
     */
    @Select("SELECT COUNT(1) FROM t_sys_user WHERE dept_id = #{deptId} AND deleted = 0")
    long selectUserCountByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据部门编码查询部门
     */
    @Select("SELECT * FROM t_sys_department WHERE dept_code = #{deptCode} AND deleted = 0")
    SysDepartment selectByDeptCode(@Param("deptCode") String deptCode);
}
