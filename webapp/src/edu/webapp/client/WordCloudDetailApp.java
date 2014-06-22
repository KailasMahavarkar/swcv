package edu.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.webapp.shared.WCSetting;
import edu.webapp.shared.WordCloud;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WordCloudDetailApp implements EntryPoint
{
	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final WordCloudDetailServiceAsync service = GWT.create(WordCloudDetailService.class);
	/**
	 * used for get new setting from a generated word cloud
	 */
	private WCSetting setting;
	private String inputText;
	private int id;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad()
	{
		id = -1;
		try
		{
			id = Integer.valueOf(Window.Location.getParameter("id"));
		}
		catch (NumberFormatException e)
		{
			DialogBox errorBox = AppUtils.createErrorBox(e, null);
			errorBox.center();
			errorBox.show();
			return;
		}

		service.getWordCloud(id, new AsyncCallback<WordCloud>()
		{
			public void onSuccess(WordCloud cloud)
			{
				SimplePanel panel = createPanel(cloud);
				RootPanel rPanel = RootPanel.get("cloud-div");
				rPanel.add(panel);
				rPanel.setPixelSize(cloud.getWidth() + 20, cloud.getHeight() + 20);
				rPanel.addStyleName("center");

				SimplePanel panel2 = createPanel2(cloud);
				RootPanel rPanel2 = RootPanel.get("cloud-div2");
				rPanel2.add(panel2);
				if (cloud.getWidth2() != 0)
					rPanel2.setPixelSize(cloud.getWidth2() + 20, cloud.getHeight2() + 20);
				else
					rPanel2.setPixelSize(0, 0);
				rPanel2.addStyleName("center");

				setting = cloud.getSettings();
				inputText = cloud.getSourceText();
				CaptionPanel settingArea = new SettingsPanel(setting, true).create();
				settingArea.setCaptionText("options");
				RootPanel.get("cloud-setting").add(settingArea);

				setSaveAsLinks(cloud);
			}

			public void onFailure(Throwable caught)
			{
				DialogBox errorBox = AppUtils.createErrorBox(caught, null);
				errorBox.center();
				errorBox.show();
			}
		});
		createCreateWordCloudButton();
	}

	private void createCreateWordCloudButton()
	{
		Button sendButton = Button.wrap(Document.get().getElementById("btn_create_new_wc"));
		sendButton.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				createWordCloud();
			}
		});
	}

	private void createWordCloud()
	{
		final DialogBox shadow = AppUtils.createShadow();
		shadow.center();
		shadow.show();

		final DialogBox loadingBox = AppUtils.createLoadingBox();
		loadingBox.show();
		loadingBox.center();
		service.updateWordCloud(id, inputText, setting, new AsyncCallback<WordCloud>()
		{
			public void onSuccess(WordCloud result)
			{
				loadingBox.hide();
				shadow.hide();
				Window.Location.assign("/cloud.html?id=" + result.getId());
			}

			public void onFailure(Throwable caught)
			{
				loadingBox.hide();
				DialogBox errorBox = AppUtils.createErrorBox(caught, shadow);
				errorBox.center();
				errorBox.show();
			}
		});
	}

	private SimplePanel createPanel(WordCloud cloud)
	{
		SimplePanel panel = new SimplePanel();
		panel.setPixelSize(cloud.getWidth() + 20, cloud.getHeight() + 20);
		panel.addStyleName("center");
		panel.add(new HTML(cloud.getSvg()));
		return panel;
	}

	private SimplePanel createPanel2(WordCloud cloud)
	{
		SimplePanel panel = new SimplePanel();
		panel.setPixelSize(cloud.getWidth2(), cloud.getHeight2());
		panel.addStyleName("center");
		panel.add(new HTML(cloud.getSvg2()));
		return panel;
	}

	private void setSaveAsLinks(WordCloud cloud)
	{
		Anchor link = Anchor.wrap(Document.get().getElementById("save-as-svg"));
		link.setHref("/cloud/download?ft=svg&id=" + cloud.getId());

		Anchor linkPNG = Anchor.wrap(Document.get().getElementById("save-as-png"));
		linkPNG.setHref("/cloud/download?ft=png&id=" + cloud.getId());

		Anchor linkPDF = Anchor.wrap(Document.get().getElementById("save-as-pdf"));
		linkPDF.setHref("/cloud/download?ft=pdf&id=" + cloud.getId());
	}

}