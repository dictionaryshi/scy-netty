package com.scy.netty.server.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.scy.core.format.MessageUtil;
import com.scy.core.json.JsonUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.job.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : shichunyang
 * Date    : 2022/4/12
 * Time    : 12:04 下午
 * ---------------------------------------
 * Desc    : HttpServerHandler
 */
@Slf4j
@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final HttpServerHandler INSTANCE = new HttpServerHandler();

    private HttpServerHandler() {
    }

    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = ThreadPoolUtil.getThreadPool("job", 10, 200, 1024);

    public static final ThreadPoolExecutor JOB_NETTY_POOL = ThreadPoolUtil.getThreadPool("job-netty", 10, 200, 500);

    public static final TypeReference<JobParam> JOB_PARAM_TYPE_REFERENCE = new TypeReference<JobParam>() {
    };

    private static final ConcurrentMap<Integer, Job> JOB_MAP = new ConcurrentHashMap<>();

    public static Job registerJob(int jobId, JobHandler handler, String removeOldReason) {
        Job newJob = new Job(jobId, handler);

        THREAD_POOL_EXECUTOR.execute(newJob);

        Job oldJob = JOB_MAP.put(jobId, newJob);
        if (Objects.nonNull(oldJob)) {
            oldJob.toStop(removeOldReason);
        }

        return newJob;
    }

    public static Job removeJob(int jobId, String removeOldReason) {
        Job oldJob = JOB_MAP.remove(jobId);
        if (Objects.nonNull(oldJob)) {
            oldJob.toStop(removeOldReason);
            return oldJob;
        }
        return null;
    }

    public static Job loadJob(int jobId) {
        return JOB_MAP.get(jobId);
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);

        HttpMethod httpMethod = fullHttpRequest.method();

        HttpHeaders headers = fullHttpRequest.headers();

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());

        String requestData = fullHttpRequest.content().toString(CharsetUtil.UTF_8);

        JOB_NETTY_POOL.execute(() -> {
            ResponseResult<Boolean> responseResult = process(httpMethod, headers, queryStringDecoder, requestData);
            writeResponse(channelHandlerContext, keepAlive, JsonUtil.object2Json(responseResult));
        });
    }

    private ResponseResult<Boolean> process(HttpMethod httpMethod, HttpHeaders headers, QueryStringDecoder queryStringDecoder, String requestData) {
        if (HttpMethod.POST != httpMethod) {
            return ResponseResult.error(JobContext.CODE_FAIL, "HttpMethod not support", Boolean.FALSE);
        }

        String uri = queryStringDecoder.path();
        try {
            JobParam jobParam = JsonUtil.json2Object(requestData, JOB_PARAM_TYPE_REFERENCE);
            if (Objects.isNull(jobParam)) {
                return ResponseResult.error(JobContext.CODE_FAIL, MessageUtil.format("invalid jobParam", "requestData", requestData), Boolean.FALSE);
            }

            if ("/beat".equals(uri)) {
                return Executor.beat();
            }

            if ("/idleBeat".equals(uri)) {
                return Executor.idleBeat(jobParam);
            }

            if ("/run".equals(uri)) {
                return Executor.run(jobParam);
            }

            if ("/kill".equals(uri)) {
                return Executor.kill(jobParam);
            }

            return ResponseResult.error(JobContext.CODE_FAIL, MessageUtil.format("invalid request", "uri", uri), Boolean.FALSE);
        } catch (Exception e) {
            log.error(MessageUtil.format("Executor error", e));
            return ResponseResult.error(JobContext.CODE_FAIL, MessageUtil.format("Executor error", e), Boolean.FALSE);
        }
    }

    private void writeResponse(ChannelHandlerContext channelHandlerContext, boolean keepAlive, String data) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(data, CharsetUtil.UTF_8));

        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.toString());

        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());

        if (keepAlive) {
            fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        channelHandlerContext.writeAndFlush(fullHttpResponse);
    }
}
