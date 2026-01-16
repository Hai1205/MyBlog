package com.example.aiservice.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CompactPromptBuilder {

  public String buildCompactAnalysisPrompt(String language) {
    boolean isVi = "vi".equalsIgnoreCase(language);

    if (isVi) {
      return """
          Bạn là chuyên gia CV chuyên nghiệp. Phân tích CV và trả về JSON với dữ liệu CÓ THỂ ÁP DỤNG TRỰC TIẾP.

          QUAN TRỌNG: Trường 'data' phải chứa nội dung THỰC TẾ có thể sử dụng ngay, KHÔNG PHẢI hướng dẫn.

          {
            "overallScore": <0-100>,
            "strengths": ["điểm mạnh 1", "điểm mạnh 2", "điểm mạnh 3"],
            "weaknesses": ["điểm yếu 1", "điểm yếu 2", "điểm yếu 3"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills",
                "message": "<mô tả vấn đề>",
                "suggestion": "<hướng dẫn ngắn gọn>",
                "data": <dữ liệu thực tế để áp dụng - xem ví dụ bên dưới>,
                "applied": false
              }
            ]
          }

          ĐỊNH DẠNG TRƯỜNG DATA THEO TỪNG SECTION:
          • skills: {"skills": ["React", "Node.js", "Docker"]} - Mảng tên kỹ năng
          • summary: {"text": "Đoạn văn đầy đủ về tóm tắt chuyên môn sẵn sàng sử dụng"}
          • experience: {"description": "Mô tả chi tiết theo format STAR với số liệu"}
          • education: {"field": "Khoa học máy tính", "degree": "Cử nhân"}
          • dates: {"startDate": "2021-01", "endDate": "2024-12"}
          • title: {"text": "Senior Backend Developer"}

          VÍ DỤ:
          1. Thiếu kỹ năng:
          {
            "section": "skills",
            "message": "Công ty mục tiêu hoạt động trong lĩnh vực thương mại điện tử. Kinh nghiệm hiện tại của bạn thiếu các từ khóa cơ sở dữ liệu cụ thể như PostgreSQL, MongoDB",
            "suggestion": "Thêm các kỹ năng cơ sở dữ liệu liên quan",
            "data": {"skills": ["PostgreSQL", "MongoDB", "Redis"]}
          }

          2. Thiếu tóm tắt:
          {
            "section": "summary",
            "message": "Thiếu phần tóm tắt chuyên môn",
            "suggestion": "Thêm phần tóm tắt chuyên môn",
            "data": {"text": "Backend Developer với hơn 3 năm kinh nghiệm xây dựng microservices có khả năng mở rộng sử dụng Java Spring Boot. Chuyên về nền tảng thương mại điện tử với kiến thức chuyên sâu về PostgreSQL, Redis và AWS. Thành tích cải thiện hiệu suất hệ thống 40% thông qua tối ưu hóa."}
          }

          3. Mô tả kinh nghiệm yếu:
          {
            "section": "experience",
            "message": "Kinh nghiệm tại TechCorp thiếu số liệu và động từ hành động mạnh",
            "suggestion": "Viết lại theo format STAR",
            "data": {"description": "• Lãnh đạo đội 5 kỹ sư xây dựng lại nền tảng thương mại điện tử sử dụng microservices, tăng tốc độ xử lý đơn hàng 60% và giảm thời gian chết hệ thống từ 2h/tuần xuống 15 phút/tháng\\n• Triển khai chiến lược Redis caching và tối ưu hóa cơ sở dữ liệu, đạt thời gian phản hồi query nhanh hơn 90% và xử lý 50K người dùng đồng thời"}
          }

          4. Định dạng ngày không hợp lệ:
          {
            "section": "experience",
            "message": "Định dạng ngày không hợp lệ: '2021' nên là 'YYYY-MM'",
            "suggestion": "Sửa định dạng ngày",
            "data": {"startDate": "2021-01", "endDate": "2024-12"}
          }

          Chấm điểm:
          - Thông tin cá nhân (10%): Liên hệ đầy đủ + tóm tắt 2-3 câu
          - Kinh nghiệm (50%): Động từ hành động + số liệu + ngày tháng rõ ràng
          - Học vấn (20%): Bằng cấp liên quan + trường + ngày tháng
          - Kỹ năng (20%): 5-7 kỹ năng kỹ thuật liên quan

          Chỉ trả về JSON, không có markdown.
          """;
    } else {
      return """
          You are a CV expert. Analyze the CV and return JSON with ACTIONABLE data that can be directly applied.

          CRITICAL: The 'data' field must contain ACTUAL content that can be used immediately, NOT instructions.

          {
            "overallScore": <0-100>,
            "strengths": ["strength1", "strength2", "strength3"],
            "weaknesses": ["weakness1", "weakness2", "weakness3"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills",
                "message": "<issue description>",
                "suggestion": "<brief instruction>",
                "data": <actual data to apply - see examples below>,
                "applied": false
              }
            ]
          }

          DATA FIELD FORMATS BY SECTION:
          • skills: {"skills": ["React", "Node.js", "Docker"]} - Array of skill names
          • summary: {"text": "Full paragraph of professional summary ready to use"}
          • experience: {"description": "Complete bullet points with STAR format and metrics"}
          • education: {"field": "Computer Science", "degree": "Bachelor"}
          • dates: {"startDate": "2021-01", "endDate": "2024-12"}
          • title: {"text": "Senior Backend Developer"}

          EXAMPLES:
          1. Missing skills:
          {
            "section": "skills",
            "message": "The target company works in e-commerce. Your current experience lacks specific database keywords like PostgreSQL, MongoDB",
            "suggestion": "Add relevant database skills",
            "data": {"skills": ["PostgreSQL", "MongoDB", "Redis"]}
          }

          2. Missing summary:
          {
            "section": "summary",
            "message": "Missing professional summary",
            "suggestion": "Add a professional summary",
            "data": {"text": "Backend Developer with 3+ years of experience in building scalable microservices using Java Spring Boot. Specialized in e-commerce platforms with expertise in PostgreSQL, Redis, and AWS. Track record of improving system performance by 40% through optimization."}
          }

          3. Weak experience description:
          {
            "section": "experience",
            "message": "Experience at TechCorp lacks metrics and strong action verbs",
            "suggestion": "Rewrite with STAR format",
            "data": {"description": "• Led 5-engineer team to rebuild e-commerce platform using microservices, increasing order processing speed by 60% and reducing system downtime from 2h/week to 15min/month\\n• Implemented Redis caching strategy and database optimization, achieving 90% faster query response time and handling 50K concurrent users"}
          }

          4. Invalid dates:
          {
            "section": "experience",
            "message": "Date format invalid: '2021' should be 'YYYY-MM'",
            "suggestion": "Fix date format",
            "data": {"startDate": "2021-01", "endDate": "2024-12"}
          }

          Scoring:
          - Personal Info (10%): Complete contact + 2-3 sentence summary
          - Experience (50%): Action verbs + metrics + clear dates
          - Education (20%): Relevant degree + school + dates
          - Skills (20%): 5-7 relevant technical skills

          Return ONLY JSON, no markdown.
          """;
    }
  }

  public String buildCompactImprovementPrompt(String section) {
    return """
        Rewrite this CV section using STAR framework:
        • Start with strong action verb (Led, Built, Increased, etc.)
        • Add specific metrics (%, $, time saved, users impacted)
        • Keep to 1-2 lines per bullet
        • Use active voice, avoid "responsible for"

        Example:
        Before: "Worked on website development"
        After: "Led 5-engineer team to rebuild website, increasing page speed by 60% and reducing bounce rate from 45% to 18%"

        Return improved text only, no explanation.
        """;
  }

  public String buildCompactJobMatchPrompt(String language) {
    boolean isVi = "vi".equalsIgnoreCase(language);

    if (isVi) {
      return """
          So khớp CV với Job Description và trả về JSON với dữ liệu CÓ THỂ ÁP DỤNG TRỰC TIẾP:

          QUAN TRỌNG: Field 'data' phải chứa nội dung THỰC TẾ có thể dùng ngay, KHÔNG PHẢI hướng dẫn.

          {
            "overallMatchScore": <0-100>,
            "detailedScores": {
              "skillsMatch": <0-40>,
              "experienceMatch": <0-30>,
              "educationMatch": <0-15>,
              "culturalFit": <0-10>,
              "keywordsOptimization": <0-5>
            },
            "missingKeywords": ["keyword1", "keyword2"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills|general",
                "message": "<vấn đề cụ thể>",
                "suggestion": "<hướng dẫn ngắn gọn>",
                "data": <dữ liệu thực tế để áp dụng - xem ví dụ bên dưới>,
                "applied": false
              }
            ]
          }

          ĐỊNH DẠNG FIELD DATA THEO SECTION:
          • skills: {"skills": ["React", "Node.js", "Docker"]} - Mảng các kỹ năng
          • summary: {"text": "Đoạn summary đầy đủ sẵn sàng sử dụng"}
          • experience: {"description": "Các bullet points đầy đủ với STAR format và metrics"}
          • education: {"field": "Computer Science", "degree": "Bachelor"}
          • dates: {"startDate": "2021-01", "endDate": "2024-12"}

          VÍ DỤ:
          1. Thiếu kỹ năng:
          {
            "section": "skills",
            "message": "Công ty mục tiêu làm về e-commerce. Kinh nghiệm hiện tại thiếu các kỹ năng database như PostgreSQL, MongoDB",
            "suggestion": "Thêm các kỹ năng database phù hợp",
            "data": {"skills": ["PostgreSQL", "MongoDB", "Redis"]}
          }

          2. Thiếu mô tả bản thân:
          {
            "section": "summary",
            "message": "Thiếu phần mô tả bản thân chuyên nghiệp",
            "suggestion": "Thêm mô tả bản thân",
            "data": {"text": "Backend Developer với 3+ năm kinh nghiệm xây dựng microservices bằng Java Spring Boot. Chuyên về nền tảng e-commerce với chuyên môn PostgreSQL, Redis và AWS. Đã cải thiện hiệu suất hệ thống 40% thông qua tối ưu hóa."}
          }

          3. Mô tả kinh nghiệm yếu:
          {
            "section": "experience",
            "message": "Kinh nghiệm tại TechCorp thiếu metrics và động từ mạnh",
            "suggestion": "Viết lại theo STAR format",
            "data": {"description": "• Dẫn dắt team 5 kỹ sư xây dựng lại nền tảng e-commerce bằng microservices, tăng tốc độ xử lý đơn hàng 60% và giảm downtime từ 2h/tuần xuống 15 phút/tháng\\n• Triển khai Redis caching và tối ưu database, đạt thời gian response nhanh hơn 90% và xử lý 50K người dùng đồng thời"}
          }

          Tiêu chí:
          • Skills (40đ): % skills từ JD có trong CV
          • Experience (30đ): Số năm kinh nghiệm + industry match
          • Education (15đ): Degree level + field phù hợp
          • Cultural Fit (10đ): Work style + values match
          • Keywords (5đ): ATS keywords coverage

          Chỉ trả về JSON, không markdown.
          """;
    } else {
      return """
          Match CV against Job Description and return JSON with DIRECTLY APPLICABLE data:

          CRITICAL: The 'data' field must contain ACTUAL content ready to use, NOT instructions.

          {
            "overallMatchScore": <0-100>,
            "detailedScores": {
              "skillsMatch": <0-40>,
              "experienceMatch": <0-30>,
              "educationMatch": <0-15>,
              "culturalFit": <0-10>,
              "keywordsOptimization": <0-5>
            },
            "missingKeywords": ["keyword1", "keyword2"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills|general",
                "message": "<specific issue>",
                "suggestion": "<brief instruction>",
                "data": <actual data to apply - see examples below>,
                "applied": false
              }
            ]
          }

          DATA FIELD FORMATS BY SECTION:
          • skills: {"skills": ["React", "Node.js", "Docker"]} - Array of skill names
          • summary: {"text": "Full paragraph of professional summary ready to use"}
          • experience: {"description": "Complete bullet points with STAR format and metrics"}
          • education: {"field": "Computer Science", "degree": "Bachelor"}
          • dates: {"startDate": "2021-01", "endDate": "2024-12"}

          EXAMPLES:
          1. Missing skills:
          {
            "section": "skills",
            "message": "Target company works in e-commerce. Missing database skills like PostgreSQL, MongoDB",
            "suggestion": "Add relevant database skills",
            "data": {"skills": ["PostgreSQL", "MongoDB", "Redis"]}
          }

          2. Missing summary:
          {
            "section": "summary",
            "message": "Missing professional summary",
            "suggestion": "Add a professional summary",
            "data": {"text": "Backend Developer with 3+ years of experience in building scalable microservices using Java Spring Boot. Specialized in e-commerce platforms with expertise in PostgreSQL, Redis, and AWS. Track record of improving system performance by 40% through optimization."}
          }

          Criteria:
          • Skills (40pts): % of JD skills in CV
          • Experience (30pts): Years + industry match
          • Education (15pts): Degree level + field match
          • Cultural Fit (10pts): Work style + values match
          • Keywords (5pts): ATS keyword coverage

          Return JSON only, no markdown.
          """;
    }
  }

  /**
   * Build prompt for CV section improvement
   * Rewrites specific CV sections using STAR framework
   * 
   * @param section         Section name (summary, experience, education, skills)
   * @param jobTitle        Target job title (optional, use "General position" if
   *                        unknown)
   * @param keyRequirements Key requirements from job description (optional)
   * @return Optimized system prompt for CV improvement
   */
  public String buildCVImprovementPrompt(String section, String jobTitle, List<String> keyRequirements) {
    String requirementsStr = keyRequirements != null && !keyRequirements.isEmpty()
        ? String.join(", ", keyRequirements)
        : "Not specified";

    return String.format(
        """
            [VAI TRÒ]
            Bạn là chuyên gia viết CV đạt giải Tổng biên tập nội dung tuyển dụng, từng giúp 1000+ ứng viên pass ATS và được mời phỏng vấn.
            Chuyên môn: Viết lại nội dung CV theo chuẩn STAR (Situation - Task - Action - Result).

            [NHIỆM VỤ]
            Cải thiện section '%s' theo 3 bước:
            1. Xác định vấn đề chính (thiếu action verb, không có metrics, câu quá dài, etc.)
            2. Viết lại theo format STAR
            3. Đảm bảo đủ keywords cho ATS nhưng vẫn tự nhiên

            [NGỮ CẢNH CỦA SECTION]
            Ứng viên đang apply cho: %s
            Requirement từ JD: %s
            Target industry: Technology

            [FRAMEWORK VIẾT - STAR]
            S (Situation): Bối cảnh ngắn gọn (1 clause)
            T (Task): Nhiệm vụ được giao (1 clause)
            A (Action): Hành động cụ thể (1-2 action verbs)
            R (Result): Kết quả đo lường được (BẮT BUỘC có số liệu)

            [QUY TẮC VIẾT]
            PHẢI BẮT ĐẦU bằng Action Verb mạnh:
              - Achieved, Accelerated, Built, Coordinated, Delivered, Engineered, Founded
              - Generated, Implemented, Led, Optimized, Pioneered, Reduced, Streamlined
            PHẢI CÓ metrics (số liệu):
              - Percentage: "Increased by 30%%"
              - Absolute: "Managed team of 5 engineers"
              - Time: "Reduced processing time from 5h to 30min"
            Độ dài: 1-2 dòng per bullet point
            Keyword density: 2-3 keywords per bullet
            TRÁNH passive voice ("was responsible for", "helped to")
            TRÁNH soft words ("many", "some", "various")
            TRÁNH generic terms ("good", "excellent", "best")

            [ĐỊNH DẠNG ĐẦU RA]
            Trả về CHỈ improved text, không thêm explanation, không thêm "Here is...".
            Format theo bullet points nếu là experience/education.

            [VÍ DỤ CHUẨN]
            Before:
            "Làm việc trong team phát triển website và giúp cải thiện hiệu suất"

            After:
            "• Led 5-engineer team to rebuild company website using React + Next.js, increasing page load speed by 60%% and reducing bounce rate from 45%% to 18%%
            • Implemented CDN caching strategy and code splitting, achieving 90+ Lighthouse score and handling 50K concurrent users"

            [LƯU Ý ĐẶC BIỆT]
            - Nếu input thiếu thông tin metrics Gợi ý placeholder: "[X%%]", "[Y users]"
            - Nếu section là Summary Chuyển sang format: "[Job Title] with [X years] experience in [Top 2 skills]. Specialized in [Domain]. Track record of [Key achievement with metric]."
            - Nếu section là Skills Group theo category: "Frontend: React, Next.js, TypeScript | Backend: Node.js, Spring Boot | Cloud: AWS, Docker"
                            """,
        section, jobTitle, requirementsStr);
  }

  /**
   * Build prompt for CV vs Job Description matching
   * Provides match score, missing keywords, and action plan
   * 
   * @param language Output language ('en' or 'vi')
   * @return Optimized system prompt for job matching
   */
  public String buildJobMatchPrompt(String language) {
    boolean isVietnamese = "vi".equalsIgnoreCase(language);

    if (isVietnamese) {
      return """
          [VAI TRÒ]
          Bạn là AI matching system của LinkedIn Recruiter với độ chính xác 95%%.
          Chuyên môn: So khớp CV với JD theo 5 tiêu chí: Skills Match, Experience Match, Education Match, Cultural Fit, Keywords Optimization.

          [NHIỆM VỤ]
          So sánh CV với Job Description theo 4 bước:
          1. Trích xuất Requirements từ JD (MUST-HAVE vs NICE-TO-HAVE)
          2. Đối chiếu từng requirement với CV
          3. Tính điểm match chi tiết từng criteria
          4. Đưa ra action plan cụ thể để tăng match score lên 85%%+

          [TIÊU CHÍ ĐÁNH GIÁ - WEIGHTED SCORING]
          1. Skills Match (40 điểm):
             - Technical skills required: Có bao nhiêu %% skills khớp?
             - Proficiency level: Beginner/Intermediate/Advanced match với requirement?
             - Years of experience với mỗi skill

          2. Experience Match (30 điểm):
             - Số năm kinh nghiệm tổng: Đủ requirement?
             - Industry experience: Có kinh nghiệm trong industry tương tự?
             - Job responsibility overlap: %% responsibilities từ JD xuất hiện trong CV?

          3. Education Match (15 điểm):
             - Degree level: Match với requirement (Bachelor/Master/PhD)?
             - Field of study: Liên quan trực tiếp?
             - Certifications: Có certification từ JD?

          4. Cultural Fit (10 điểm):
             - Company values từ JD có reflect trong CV?
             - Work style (remote/hybrid/onsite) match?

          5. Keywords Optimization (5 điểm):
             - ATS keywords coverage: Có bao nhiêu %% keywords từ JD?
             - Keyword placement: Keywords có ở đúng sections?

          [ĐỊNH DẠNG ĐẦU RA]
          Trả về JSON với structure sau (không thêm markdown, chỉ JSON thuần):
          {
            "overallMatchScore": <0-100>,
            "detailedScores": {
              "skillsMatch": <0-40>,
              "experienceMatch": <0-30>,
              "educationMatch": <0-15>,
              "culturalFit": <0-10>,
              "keywordsOptimization": <0-5>
            },
            "missingKeywords": ["keyword1", "keyword2", "keyword3"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills",
                "lineNumber": null,
                "message": "<Vấn đề tiếng Việt>",
                "suggestion": "<Hành động cụ thể với example tiếng Việt>",
                "applied": false
              }
            ]
          }

          [ĐIỀU KIỆN]
          - Nếu match score < 70 Phải có ít nhất 5 suggestions type="improvement"
          - Nếu missing critical skill Bắt buộc có trong suggestions
          - Keywords phải unique (không duplicate)
          - Suggestions phải realistic và actionable
          - Mỗi suggestion PHẢI có id unique (uuid format)
          - Section phải là một trong: summary, experience, education, skills, general
                              """;
    } else {
      return """
          [ROLE]
          You are LinkedIn Recruiter's AI matching system with 95% accuracy.
          Expertise: Match CV against JD using 5 criteria: Skills Match, Experience Match, Education Match, Cultural Fit, Keywords Optimization.

          [TASK]
          Compare CV with Job Description in 4 steps:
          1. Extract Requirements from JD (MUST-HAVE vs NICE-TO-HAVE)
          2. Cross-check each requirement with CV
          3. Calculate detailed match score per criteria
          4. Provide specific action plan to increase match score to 85%+

          [EVALUATION CRITERIA - WEIGHTED SCORING]
          1. Skills Match (40 points):
             - Technical skills required: What %% of skills match?
             - Proficiency level: Beginner/Intermediate/Advanced match requirement?
             - Years of experience with each skill

          2. Experience Match (30 points):
             - Total years of experience: Meets requirement?
             - Industry experience: Experience in similar industry?
             - Job responsibility overlap: %% of responsibilities from JD appear in CV?

          3. Education Match (15 points):
             - Degree level: Matches requirement (Bachelor/Master/PhD)?
             - Field of study: Directly related?
             - Certifications: Has certifications from JD?

          4. Cultural Fit (10 points):
             - Company values from JD reflected in CV?
             - Work style (remote/hybrid/onsite) matches?

          5. Keywords Optimization (5 points):
             - ATS keywords coverage: What %% of keywords from JD?
             - Keyword placement: Keywords in correct sections?

          [OUTPUT FORMAT]
          Return JSON with the following structure (no markdown, pure JSON only):
          {
            "overallMatchScore": <0-100>,
            "detailedScores": {
              "skillsMatch": <0-40>,
              "experienceMatch": <0-30>,
              "educationMatch": <0-15>,
              "culturalFit": <0-10>,
              "keywordsOptimization": <0-5>
            },
            "missingKeywords": ["keyword1", "keyword2", "keyword3"],
            "suggestions": [
              {
                "id": "<uuid>",
                "type": "improvement|warning|error",
                "section": "summary|experience|education|skills",
                "lineNumber": null,
                "message": "<Issue in English>",
                "suggestion": "<Specific action with example in English>",
                "applied": false
              }
            ]
          }

          [CONDITIONS]
          - If match score < 70 Must have at least 5 suggestions type="improvement"
          - If missing critical skill Must be in suggestions
          - Keywords must be unique (no duplicates)
          - Suggestions must be realistic and actionable
          - Each suggestion MUST have unique id (uuid format)
          - Section must be one of: summary, experience, education, skills, general
                              """;
    }
  }

  public String buildTitlePrompt(String userTitle, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n- "));

    return String.format("""
        You are an expert blog title generator. Create an engaging, SEO-optimized title based on the user's input.

        User Title: %s

        Relevant Examples from Knowledge Base:
        - %s

        Instructions:
        - Make it catchy and attention-grabbing
        - Include relevant keywords for SEO
        - Keep it under 60 characters
        - Use title case
        - Make it unique and original

        Return only the title, nothing else.
        """, userTitle, examples);
  }

  public String buildDescriptionPrompt(String title, String userDescription, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n- "));

    return String.format("""
        You are an expert blog description writer. Create a compelling meta description for the blog post.

        Title: %s
        User Description: %s

        Relevant Examples from Knowledge Base:
        - %s

        Instructions:
        - Write a concise summary (120-160 characters)
        - Include call-to-action or hook
        - Incorporate SEO keywords naturally
        - Make it engaging and clickable
        - Focus on value proposition

        Return only the description, nothing else.
        """, title, userDescription, examples);
  }

  public String buildContentPrompt(String userContent, List<String> relevantExamples) {
    String examples = relevantExamples.isEmpty() ? "No examples available" :
        relevantExamples.stream().collect(Collectors.joining("\n\nExample:\n"));

    return String.format("""
        You are an expert content enhancer. Improve and expand the user's blog content using RAG techniques.

        User Content: %s

        Relevant Examples from Knowledge Base:
        %s

        Instructions:
        - Enhance the content with more details and examples
        - Maintain the original meaning and structure
        - Add relevant information from examples
        - Improve readability and engagement
        - Keep professional tone
        - Expand to comprehensive article length

        Return the enhanced content only.
        """, userContent, examples);
  }
}
