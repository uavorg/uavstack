/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.apphub.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * ValidataCodeTools description: 验证码工具类
 *
 * @author lbay
 */
public class CaptchaTools {

    private ISystemLogger logger = SystemLogger.getLogger(CaptchaTools.class);
    private String vcAnswerKeyName = "VALIDATACODE_ANSWER";
    private static LinkedBlockingQueue<ApphubBufferedImage> vcObjs = new LinkedBlockingQueue<ApphubBufferedImage>(200);

    /**
     * 验证结果
     * 
     * @param session
     * @param input
     * @return
     */
    public boolean checkVCAnswer(HttpSession session, String input) {

        return input.equals(session.getAttribute(vcAnswerKeyName));
    }

    public void newVC(HttpSession session, OutputStream outputStream) {

        try {
            appendVC();
            ApphubBufferedImage obj = vcObjs.poll(5, TimeUnit.SECONDS);
            if (null == obj) {
                throw new NullPointerException("没有获取到验证码，等待超时。");
            }
            ImageIO.write(obj, "png", outputStream);
            session.setAttribute(vcAnswerKeyName, obj.getAnswer());
        }
        catch (Exception e) {
            logger.err(this, e.getMessage(), e);
        }
        finally {
            try {
                if (null != outputStream) {
                    outputStream.flush();
                    outputStream.close();
                }
            }
            catch (IOException e1) {
                logger.err(this, e1.getMessage(), e1);
            }
        }
    }

    /**
     * 追加验证码
     */
    private void appendVC() {

        if (!vcObjs.isEmpty()) {
            return;
        }

        logger.info(this, "<---------------------------开始生产新的一批验证码:new 50，max 200--------------------------->");
        new Thread() {

            @Override
            public void run() {

                int queueAppendSize = 50;
                do {
                    createVC();
                    queueAppendSize--;
                }
                while (queueAppendSize > 0);
            }

        }.start();

    }

    private void createVC() {

        // 创建验证码
        try {
            // 图片的宽度。
            int width = 70;
            // 图片的高度。
            int height = 30;
            // 验证码干扰线数量
            int lineCount = 130;

            // 验证码图片Buffer
            ApphubBufferedImage buffImg = new ApphubBufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = buffImg.createGraphics();
            // 生成随机数
            Random random = new Random();
            // 将图像填充为白色
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            // 创建字体
            String[] fontName = new String[] { "STYLE_BOLD", "STYLE_ITALIC", "STYLE_UNDERLINED" };
            Font font = new Font(fontName[random.nextInt(fontName.length)], random.nextInt(3), 20);
            g.setFont(font);

            // 绘制干扰线
            for (int i = 0; i < lineCount; i++) {
                int xs = random.nextInt(width);
                int ys = random.nextInt(height);
                int xe = xs + random.nextInt(width / 8);
                int ye = ys + random.nextInt(height / 8);
                int red = random.nextInt(255);
                int green = random.nextInt(255);
                int blue = random.nextInt(255);
                g.setColor(new Color(red, green, blue));
                g.drawLine(xs, ys, xe, ye);
            }

            String[] vc = createOperationVC();

            // 设置字体颜色
            g.setColor(getRandomColor());

            // 显示字体位置
            int xWidth = random.nextInt(8);
            int yHeight = 15 + random.nextInt(15);

            // 设置字体位置
            g.drawString(vc[0], xWidth, yHeight);
            buffImg.setAnswer(vc[1]);
            vcObjs.add(buffImg);
        }
        catch (Exception e) {
            logger.err(this, e.getMessage(), e);
        }
    }

    /**
     * 创建验证码:运算公式(加减乘除)
     * 
     * @return ： [公式,答案]
     */
    public String[] createOperationVC() {

        String[] operSymbols = new String[] { "+", "减", "×", "除" };
        Random random = new Random();
        String operSymbol = operSymbols[random.nextInt(4)];
        int number1 = 0, number2 = 0, number3 = 0;

        if ("+".equals(operSymbol)) {
            number1 = random.nextInt(100);
            number2 = random.nextInt(100);
            number3 = number1 + number2;
        }
        else if ("减".equals(operSymbol)) {
            number1 = random.nextInt(100);
            do {
                number2 = random.nextInt(100);
            }
            while (number1 < number2);
            number3 = number1 - number2;
        }
        else if ("×".equals(operSymbol)) {
            number1 = random.nextInt(10);
            number2 = random.nextInt(10);
            number3 = number1 * number2;
        }
        else if ("除".equals(operSymbol)) {
            do {
                number2 = random.nextInt(10);
            }
            while (number2 == 0);
            number1 = number2 * random.nextInt(10);
            number3 = number1 / number2;

        }

        return new String[] { Integer.toString(number1) + operSymbol + Integer.toString(number2) + "=?",
                Integer.toString(number3) };
    }

    /** 获取随机颜色 */
    private Color getRandomColor() {

        Random random = new Random();
        int r = 200 + random.nextInt(55);
        int g = 10 + random.nextInt(20);
        int b = 10 + random.nextInt(20);
        return new Color(r, g, b);
    }

    public class ApphubBufferedImage extends BufferedImage {

        private String answer;

        public ApphubBufferedImage(int width, int height, int imageType) {
            super(width, height, imageType);
        }

        public ApphubBufferedImage(int width, int height, int imageType, IndexColorModel cm) {

            super(width, height, imageType, cm);
        }

        public ApphubBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
                Properties properties) {
            super(cm, raster, isRasterPremultiplied, properties);
        }

        public String getAnswer() {

            return answer;
        }

        public void setAnswer(String answer) {

            this.answer = answer;
        }

    }

}
