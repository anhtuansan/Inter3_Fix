package dal;

import context.DBContext;
import dto.LessonDTO;
import dto.LessonSubjectDTO;
import dto.OwnerDTO;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Lesson;

public class LessonDAO extends DBContext {

    private static LessonDAO instance;
    private static Object lockPad = new Object();

    private LessonDAO() {
    }

    /**
     * Provides a global point of access to the UserDAO instance. Implements
     * double-checked locking to ensure thread safety.
     *
     * @return the singleton instance of UserDAO
     */
    public static LessonDAO getInstance() {
        if (instance == null) {
            synchronized (lockPad) {
                if (instance == null) {
                    instance = new LessonDAO();
                }
            }
        }
        return instance;
    }

    public List<Lesson> getLessonsBySubjectWithPaging(int subjectId, int page, int recordPerPage, String name, Integer status, String type) {
        List<Lesson> lessons = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH PagedResults AS (")
                .append("SELECT *, ROW_NUMBER() OVER (ORDER BY LessonIndex) AS row_num ")
                .append("FROM lessons ")
                .append("WHERE id IN (SELECT lesson_id FROM subject_has_lesson WHERE subject_id=?) ");

        if (name != null && !name.isEmpty()) {
            sql.append("AND name LIKE ? ");
        }
        if (status != null) {
            sql.append("AND status = ? ");
        }
        if (type != null && !type.isEmpty()) {
            sql.append("AND type = ? ");
        }

        sql.append(") SELECT * FROM PagedResults WHERE row_num BETWEEN ? AND ? ORDER BY row_num");

        int startRow = (page - 1) * recordPerPage + 1;
        int endRow = page * recordPerPage;

        try {
            ps = connection.prepareStatement(sql.toString());

            int paramIndex = 1;
            ps.setInt(paramIndex++, subjectId);

            if (name != null && !name.isEmpty()) {
                ps.setString(paramIndex++, "%" + name + "%");
            }
            if (status != null) {
                ps.setInt(paramIndex++, status);
            }
            if (type != null && !type.isEmpty()) {
                ps.setString(paramIndex++, type);
            }

            ps.setInt(paramIndex++, startRow);
            ps.setInt(paramIndex++, endRow);

            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name2 = rs.getString(2);
                int creatorId = rs.getInt(3);
                Date updateAt = rs.getDate(4);
                Date createdAt = rs.getDate(5);
                int status2 = rs.getInt(6);
                String content = rs.getString(7);
                String media = rs.getString(8);
                int lessonIndex = rs.getInt(9);
                String type2 = rs.getString(10);

                Lesson lesson = new Lesson(id, name2, creatorId, updateAt, createdAt, status2, content, media, lessonIndex, type2);
                lessons.add(lesson);
            }
        } catch (SQLException ex) {
            Logger.getLogger(LessonDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lessons;
    }

    public int getCount(int subjectId, String name, Integer status, String type) {
        String sql = "SELECT COUNT(*) AS total FROM lessons "
                + "WHERE id IN (SELECT lesson_id FROM subject_has_lesson WHERE subject_id = ?) ";

        if (name != null && !name.isEmpty()) {
            sql += "AND name LIKE ? ";
        }
        if (status != null) {
            sql += "AND status = ? ";
        }
        if (type != null && !type.isEmpty()) {
            sql += "AND Type = ? ";
        }

        try {
            ps = connection.prepareStatement(sql.toString());
            int paramIndex = 1;
            ps.setInt(paramIndex++, subjectId);

            if (name != null && !name.isEmpty()) {
                ps.setString(paramIndex++, "%" + name + "%");
            }
            if (status != null) {
                ps.setInt(paramIndex++, status);
            }
            if (type != null && !type.isEmpty()) {
                ps.setString(paramIndex++, type);
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Handle or log exception as needed
        }

        return 0; // Default return value if no count retrieved
    }

   

    public int getIdAddCurrent() {
        String sql = "SELECT @@IDENTITY AS LastInsertedId;";

        try {
            ps = connection.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Handle or log exception as needed
        }

        return 0; // Default return value if no count retrieved
    }

    public boolean insertSubjectLesson(int subjectId, int lessonId) {
        boolean updated = false;
        String query = "insert into subject_has_lesson values(?,?);	";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, subjectId);
            ps.setInt(2, lessonId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                updated = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    public boolean updateLesson(String name, String content, String media, int lessonIndex, String type, int lessonId) {
        boolean updated = false;
        String query = "update lessons set name=? ,content=?, media=?, LessonIndex=?, Type=? "
                + "where id = ? ";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, content);
            ps.setString(3, media);
            ps.setInt(4, lessonIndex);
            ps.setString(5, type);
            ps.setInt(6, lessonId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                updated = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    public boolean updateStatus(int status, int lessonId) {
        boolean updated = false;
        String query = "update lessons set status = ? where id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, status);
            ps.setInt(2, lessonId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                updated = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    public int getUserIdBySubjectId(int subjectId) {
        String sql = "select creator_id from subjects where id = ?";

        try {
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, subjectId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // Handle or log exception as needed
        }

        return 0; // Default return value if no count retrieved
    }

    public Lesson getLessonById(int lessonId) {
        String sql = "select * from lessons where id=?";

        try {
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, lessonId);
            rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                int creator_id = rs.getInt(3);
                Date update_at = rs.getDate(4);
                Date created_at = rs.getDate(5);
                int status = rs.getInt(6);
                String content = rs.getString(7);
                String media = rs.getString(8);
                int lessonIndex = rs.getInt(9);
                String Type = rs.getString(10);
                Lesson lesson = new Lesson(id, name, creator_id, update_at, created_at, status, content, media, lessonIndex, Type);

                return lesson;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public List<LessonSubjectDTO> getLessonsBySubjectId(int subjectId, int page, int recordsPerPage, String search) {
        int start = (page - 1) * recordsPerPage + 1;
        int end = start + recordsPerPage - 1;
        List<LessonSubjectDTO> lessons = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("WITH PageResult AS (");
        query.append("  SELECT l.[id], ");
        query.append("         l.[name], ");
        query.append("         l.[content], ");
        query.append("         l.[LessonIndex], ");
        query.append("         l.[Type], ");
        query.append("         ROW_NUMBER() OVER (ORDER BY l.[LessonIndex]) AS row_num ");
        query.append("  FROM [SWP391_G6].[dbo].[lessons] AS l ");
        query.append("  INNER JOIN [dbo].[subject_has_lesson] AS sl ON sl.lesson_id = l.id ");
        query.append("  INNER JOIN [dbo].[subjects] AS s ON s.id = sl.subject_id ");
        query.append("  WHERE sl.subject_id = ?  AND (l.[name] LIKE ? OR l.[content] LIKE ? OR l.[LessonIndex] LIKE ? OR l.[Type] LIKE ?) ");
        query.append(") ");
        query.append("SELECT * FROM PageResult ");
        query.append("WHERE row_num BETWEEN ? AND ?;");
        try {
            String searchCondition = "%" + search + "%";
            ps = connection.prepareStatement(query.toString());
            ps.setInt(1, subjectId);
            ps.setString(2, searchCondition);
            ps.setString(3, searchCondition);
            ps.setString(4, searchCondition);
            ps.setString(5, searchCondition);
            ps.setInt(6, start);
            ps.setInt(7, end);
            rs = ps.executeQuery();
            while (rs.next()) {
                lessons.add(new LessonSubjectDTO(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getString(5)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lessons;
    }

    public int getNumberOfLessonsBySubjectId(int subjectId) {
        String query = "SELECT COUNT(lesson_id) AS NumberOfLessons\n"
                + "FROM [SWP391_G6].[dbo].[subject_has_lesson]\n"
                + "WHERE subject_id = ?";
        int numberOfLessons = 0;
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, subjectId);
            rs = ps.executeQuery();
            if (rs.next()) {
                numberOfLessons = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numberOfLessons;
    }

    public int getTotaPageLesson(int subjectId, String search, int recordPerPage) {
        String query = "DECLARE @TotalRecords INT;\n"
                + "DECLARE @PageSize INT = ?;\n"
                + "\n"
                + "SELECT @TotalRecords = COUNT(*)\n"
                + "FROM [SWP391_G6].[dbo].[lessons] AS l\n"
                + "INNER JOIN [dbo].[subject_has_lesson] AS sl ON sl.lesson_id = l.id\n"
                + "INNER JOIN [dbo].[subjects] AS s ON s.id = sl.subject_id\n"
                + "WHERE sl.subject_id = ? AND (l.[name] LIKE ? OR l.[content] LIKE ? OR l.[LessonIndex] LIKE ? OR l.[Type] LIKE ?);\n"
                + "\n"
                + "DECLARE @TotalPages INT;\n"
                + "SET @TotalPages = CEILING(CAST(@TotalRecords AS FLOAT) / CAST(@PageSize AS FLOAT));\n"
                + "SELECT @TotalPages";
        int totalPage = 0;
        try {
            search = "%" + search + "%";
            ps = connection.prepareStatement(query);
            ps.setInt(1, recordPerPage);
            ps.setInt(2, subjectId);
            ps.setString(3, search);
            ps.setString(4, search);
            ps.setString(5, search);
            ps.setString(6, search);
            
           rs = ps.executeQuery();
           if(rs.next()) {
               totalPage = rs.getInt(1);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalPage;
    }
    
    public List<LessonDTO> getLessonsBySubjectId(String subjectId) throws SQLException {
        List<LessonDTO> lessons = new ArrayList<>();
        String query = "SELECT l.id, l.name FROM lessons l " +
                       "JOIN subject_has_lesson shl ON l.id = shl.lesson_id " +
                       "WHERE shl.subject_id = ?";
         ps = connection.prepareStatement(query);
        ps.setString(1, subjectId);
         rs = ps.executeQuery();

        while (rs.next()) {
            lessons.add(new LessonDTO(rs.getInt("id"), rs.getString("name")));
        }

        return lessons;
    }

    
    
    
    
    
    
    
    
    
    
    
   

   

    public boolean insertLesson(String name, String content, String media, int lessonIndex, String Type, int userId) {
        boolean updated = false;
        String query = "insert into lessons values(?,?,null,GETDATE(),1,?,?,?,?,null)";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.setString(4, media);
            ps.setInt(5, lessonIndex);
            ps.setString(6, Type);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                updated = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return updated;
    }

    
  
   
  
 
   
    
    
    
    public static void main(String[] args) {
        LessonDAO lessonDAO = new LessonDAO();
        System.out.println(lessonDAO.getLessonsBySubjectWithPaging(2, 1, 5, null, null, "quiz"));
        System.out.println(lessonDAO.getCount(2, "", null, ""));
        //System.out.println(lessonDAO.insertLesson("fdsafsda", "fdsafdas", "fdasf", 3, "content", 1028));
        System.out.println(lessonDAO.getIdAddCurrent());
        System.out.println(lessonDAO.getLessonById(1));
    }
}
