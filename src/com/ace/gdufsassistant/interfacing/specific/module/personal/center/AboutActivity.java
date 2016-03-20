/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.personal.center;

import com.ace.gdufsassistant.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/**
 * @author wan
 *
 */
public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textV = new TextView(this);
		textV.setText("本软件由广外学生自主开发而成，希望能带给广大广外学子方便！\n软件使用是全免费的，如果喜欢，请分享给你的同学！\n\n版本：2.0\n作者：计算机1302班四人组"
				+ "\n作者邮箱：654630948@qq.com\n(如果有什么意见或建议，可以发邮件到这个邮箱哦！)");
		textV.setGravity(Gravity.CENTER_HORIZONTAL);
		textV.setTextSize(20);
		textV.setBackground(getResources().getDrawable(R.drawable.abg));
		setContentView(textV);
	}
}
