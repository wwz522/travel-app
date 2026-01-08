package com.tripmate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.dto.AIRoutePlanRequest;
import com.tripmate.dto.AIRoutePlanResponse;
import com.tripmate.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIRoutePlanService {

    @Value("${ai.qianwen.api-key:}")
    private String apiKey;

    @Value("${ai.qianwen.api-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIRoutePlanResponse generateRoutePlan(AIRoutePlanRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("AI服务未配置，请在application.yml中配置ai.qianwen.api-key");
        }

        try {
            // 构建提示词
            String prompt = buildPrompt(request);
            
            // 调用通义千问API
            String response = callQianwenAPI(prompt);
            
            // 解析响应
            return parseResponse(response, request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("AI路线规划失败: " + e.getMessage());
        }
    }

    private String buildPrompt(AIRoutePlanRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的旅行规划师和攻略撰写专家，拥有丰富的旅游经验和最新的旅游信息。请根据以下需求规划一条详细的旅行路线和攻略：\n\n");
        
        if (request.getOrigin() != null && !request.getOrigin().isEmpty()) {
            prompt.append("出发地：").append(request.getOrigin()).append("\n");
        }
        prompt.append("目的地：").append(request.getDestination()).append("\n");
        prompt.append("行程天数：").append(request.getDays()).append("天\n");
        prompt.append("预算范围：").append(request.getBudget()).append("元\n");
        
        if (request.getTravelMode() != null && !request.getTravelMode().isEmpty()) {
            String travelModeText = getTravelModeText(request.getTravelMode());
            prompt.append("交通方式：").append(travelModeText).append("\n");
        }
        if (request.getStayLevel() != null && !request.getStayLevel().isEmpty()) {
            String stayLevelText = getStayLevelText(request.getStayLevel());
            prompt.append("住宿档次：").append(stayLevelText).append("\n");
        }
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            prompt.append("旅行偏好：").append(request.getPreferences()).append("\n");
        }
        
        prompt.append("\n请基于最新的旅游信息、景点开放时间、门票价格、交通路线等，提供一份非常详细的旅游攻略，必须包括：\n\n");
        
        prompt.append("1. 路线标题（简洁明了，吸引人，包含出发地和目的地）\n\n");
        
        prompt.append("2. 路线描述（200-300字，概括整个行程的亮点和特色）\n\n");
        
        prompt.append("3. 详细行程安排（这是最重要的部分！必须非常详细）：\n");
        prompt.append("   请按天详细列出，每天必须包含：\n");
        prompt.append("   【第一天】\n");
        prompt.append("   - 早上（8:00-12:00）：\n");
        prompt.append("     * 具体景点名称和完整地址\n");
        prompt.append("     * 建议游览时间（如：2-3小时）\n");
        prompt.append("     * 门票价格（成人票、学生票等，如果免费要说明）\n");
        prompt.append("     * 如何到达：从出发地/酒店到景点的详细交通路线\n");
        prompt.append("       * 如果从出发地：说明乘坐什么交通工具（高铁/火车/飞机），车次/航班号，出发时间，到达时间，票价\n");
        prompt.append("       * 如果从酒店：说明地铁/公交线路，换乘站点，预计时间，费用\n");
        prompt.append("     * 景点特色和游览建议\n");
        prompt.append("   - 中午（12:00-14:00）：\n");
        prompt.append("     * 推荐餐厅名称和完整地址\n");
        prompt.append("     * 人均消费（具体金额）\n");
        prompt.append("     * 特色菜品推荐\n");
        prompt.append("     * 从景点到餐厅的交通方式（步行/公交/地铁，具体路线）\n");
        prompt.append("   - 下午（14:00-18:00）：\n");
        prompt.append("     * 具体景点名称和完整地址\n");
        prompt.append("     * 建议游览时间\n");
        prompt.append("     * 门票价格\n");
        prompt.append("     * 从午餐地点到景点的交通方式（地铁/公交线路，换乘信息，预计时间，费用）\n");
        prompt.append("     * 景点特色和游览建议\n");
        prompt.append("   - 晚上（18:00-22:00）：\n");
        prompt.append("     * 推荐活动或餐厅/夜市\n");
        prompt.append("     * 具体地址和交通方式\n");
        prompt.append("     * 住宿建议：酒店名称、位置、价格范围（每晚）\n");
        prompt.append("     * 从景点到住宿的交通方式\n");
        prompt.append("   - 当日交通费用小计：列出所有交通费用（地铁、公交、打车等）\n");
        prompt.append("   - 当日餐饮费用小计：列出三餐费用\n");
        prompt.append("   - 当日门票费用小计：列出所有门票费用\n");
        prompt.append("   - 当日总费用小计\n");
        prompt.append("   \n");
        prompt.append("   【第二天】、【第三天】...（按同样格式详细列出）\n\n");
        
        prompt.append("4. 出行提示（5-8条实用建议，包括交通、天气、安全、购物、注意事项等）\n\n");
        
        prompt.append("5. 预算分解（必须非常详细，包含所有费用）：\n");
        if (request.getOrigin() != null && !request.getOrigin().isEmpty()) {
            prompt.append("   - 往返大交通费用：\n");
            prompt.append("     * 去程：从").append(request.getOrigin()).append("到").append(request.getDestination());
            if (request.getTravelMode() != null) {
                prompt.append("（").append(getTravelModeText(request.getTravelMode())).append("）");
            }
            prompt.append("，具体车次/航班，出发时间，到达时间，票价（二等座/经济舱等）\n");
            prompt.append("     * 返程：从").append(request.getDestination()).append("到").append(request.getOrigin());
            if (request.getTravelMode() != null) {
                prompt.append("（").append(getTravelModeText(request.getTravelMode())).append("）");
            }
            prompt.append("，具体车次/航班，出发时间，到达时间，票价\n");
            prompt.append("     * 往返交通费用小计\n");
        }
        prompt.append("   - 当地交通费用：每天的地铁、公交、打车等费用明细，最后汇总\n");
        prompt.append("   - 住宿费用：每晚价格 × 天数，说明住宿档次、位置、酒店类型\n");
        prompt.append("   - 餐饮费用：每天三餐的预算明细，最后汇总\n");
        prompt.append("   - 门票费用：每个景点的门票价格明细（成人票、学生票等），最后汇总\n");
        prompt.append("   - 其他费用：购物、娱乐、保险、小费等\n");
        prompt.append("   - 总计：所有费用相加的总金额，并说明是否在预算范围内\n\n");
        
        prompt.append("请以JSON格式返回，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"路线标题\",\n");
        prompt.append("  \"description\": \"路线描述（200-300字）\",\n");
        prompt.append("  \"itinerary\": \"详细行程（按上述格式详细列出，必须包含每天的时间安排、景点、交通、餐饮、门票等所有信息，格式清晰易读，用换行符分隔）\",\n");
        prompt.append("  \"tips\": \"出行提示（用逗号分隔，5-8条）\",\n");
        prompt.append("  \"budgetBreakdown\": \"预算分解（详细列出各项费用和总计，格式清晰）\"\n");
        prompt.append("}\n\n");
        
        prompt.append("重要提示：\n");
        prompt.append("- 行程安排要非常详细，包括具体时间、地点、交通换乘路线、门票价格、餐饮推荐等\n");
        prompt.append("- 交通信息要具体，包括地铁线路、公交线路、换乘站点、预计时间、费用\n");
        prompt.append("- 门票价格要准确，包括成人票、学生票、优惠票等\n");
        prompt.append("- 餐饮推荐要具体，包括餐厅名称、地址、人均消费、特色菜品\n");
        prompt.append("- 所有费用要详细列出，让用户可以直接按照攻略执行和预算\n");
        prompt.append("- itinerary字段的内容必须非常详细，不能为空或过于简略\n");
        
        return prompt.toString();
    }
    
    private String getTravelModeText(String travelMode) {
        switch (travelMode) {
            case "high-speed-rail":
                return "高铁";
            case "train":
                return "普通火车";
            case "flight":
                return "飞机";
            case "self-drive":
                return "自驾";
            case "bus":
                return "大巴";
            default:
                return travelMode;
        }
    }
    
    private String getStayLevelText(String stayLevel) {
        switch (stayLevel) {
            case "budget":
                return "经济型";
            case "comfort":
                return "舒适型";
            case "luxury":
                return "豪华型";
            default:
                return stayLevel;
        }
    }

    private String callQianwenAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("X-DashScope-SSE", "disable"); // 禁用流式输出

        // 构建消息
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", new Object[]{message});
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.7);
        parameters.put("max_tokens", 4000); // 增加到4000，详细攻略需要更多token
        parameters.put("result_format", "message"); // 指定返回格式
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-turbo");
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new BusinessException("AI API调用失败: " + response.getStatusCode() + ", 响应: " + response.getBody());
            }

            return response.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 处理4xx错误
            throw new BusinessException("AI API调用失败: " + e.getStatusCode() + ", 错误信息: " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // 处理5xx错误
            throw new BusinessException("AI服务暂时不可用: " + e.getStatusCode() + ", 请稍后重试");
        } catch (Exception e) {
            throw new BusinessException("调用AI服务时发生错误: " + e.getMessage());
        }
    }

    private AIRoutePlanResponse parseResponse(String responseBody, AIRoutePlanRequest request) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 检查是否有错误
            if (root.has("code") && !root.path("code").asText().equals("Success")) {
                String errorMsg = root.path("message").asText("未知错误");
                throw new BusinessException("AI服务返回错误: " + errorMsg);
            }
            
            // 解析响应内容
            JsonNode output = root.path("output");
            JsonNode choices = output.path("choices");
            
            // 如果没有choices，尝试直接从output获取
            String content = null;
            if (choices.isArray() && choices.size() > 0) {
                content = choices.get(0).path("message").path("content").asText();
            } else if (output.has("text")) {
                content = output.path("text").asText();
            } else if (output.has("content")) {
                content = output.path("content").asText();
            }
            
            if (content == null || content.isEmpty()) {
                throw new BusinessException("AI返回内容为空，响应: " + responseBody);
            }
            
            // 尝试解析JSON格式的响应
            try {
                JsonNode contentJson = objectMapper.readTree(content);
                AIRoutePlanResponse response = new AIRoutePlanResponse();
                response.setTitle(contentJson.path("title").asText());
                response.setDescription(contentJson.path("description").asText());
                response.setItinerary(contentJson.path("itinerary").asText());
                response.setTips(contentJson.path("tips").asText());
                response.setBudgetBreakdown(contentJson.path("budgetBreakdown").asText());
                
                // 验证必要字段
                if (response.getTitle() == null || response.getTitle().isEmpty()) {
                    throw new Exception("JSON格式不完整");
                }
                return response;
            } catch (Exception e) {
                // 如果不是JSON格式，尝试从文本中提取或使用默认值
                AIRoutePlanResponse response = new AIRoutePlanResponse();
                response.setTitle(request.getDestination() + request.getDays() + "日游");
                response.setDescription(content.length() > 200 ? content.substring(0, 200) + "..." : content);
                response.setItinerary(content);
                response.setTips("请根据实际情况调整行程，注意安全");
                response.setBudgetBreakdown("预算：" + request.getBudget() + "元（具体费用请根据实际情况调整）");
                return response;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("解析AI响应失败: " + e.getMessage() + ", 原始响应: " + responseBody.substring(0, Math.min(200, responseBody.length())));
        }
    }
}

