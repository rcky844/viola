package tipz.browservio;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
	
	private final Timer _timer = new Timer();
	
	private FloatingActionButton _fab;
	private double desktop = 0;
	private double last_desktop = 0;
	private String page_before_error = "";
	private String googleLoad = "";
	private boolean defaulterror = true;
	private double finload = 0;
	private boolean pooran = true;
	
	private LinearLayout linear_urledit;
	private LinearLayout webview_linear;
	private LinearLayout linear_urledit_text;
	private ImageView browse;
	private EditText urledit;
	private ProgressBar progmain;
	private WebView webview;
	private HorizontalScrollView hscroll_control;
	private LinearLayout linear_control;
	private LinearLayout linear_control_b0;
	private LinearLayout linear_control_b1;
	private LinearLayout linear_control_b2;
	private LinearLayout linear_control_b7;
	private LinearLayout linear_control_b3;
	private LinearLayout linear_control_b4;
	private LinearLayout linear_control_b5;
	private LinearLayout linear_control_b6;
	private LinearLayout linear_control_b9;
	private LinearLayout linear_control_b10;
	private LinearLayout linear_control_b8;
	private LinearLayout linear_control_endp;
	private ImageView back;
	private ImageView forward;
	private ImageView reload;
	private ImageView homepage_ic;
	private ImageView desktop_switch;
	private ImageView ic_clear;
	private ImageView ic_share;
	private ImageView settings;
	private ImageView history;
	private ImageView imageview1;
	private ImageView exit;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private Intent i = new Intent();
	private MediaPlayer mediaPlayer;
	private ObjectAnimator baranim = new ObjectAnimator();
	private TimerTask error_defuse;
	private TimerTask reset;
	private ObjectAnimator barrrrrr = new ObjectAnimator();
	private AlertDialog.Builder dhist;
	private TimerTask funload;
	private SharedPreferences bookmarks;
	private final int[] resID = { R.raw.win98_error };
	int from, to, times, songPosition, timesPosition=0;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		}
		else {
			initializeLogic();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		_fab = (FloatingActionButton) findViewById(R.id._fab);
		
		linear_urledit = (LinearLayout) findViewById(R.id.linear_urledit);
		webview_linear = (LinearLayout) findViewById(R.id.webview_linear);
		linear_urledit_text = (LinearLayout) findViewById(R.id.linear_urledit_text);
		browse = (ImageView) findViewById(R.id.browse);
		urledit = (EditText) findViewById(R.id.urledit);
		progmain = (ProgressBar) findViewById(R.id.progmain);
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setSupportZoom(true);
		hscroll_control = (HorizontalScrollView) findViewById(R.id.hscroll_control);
		linear_control = (LinearLayout) findViewById(R.id.linear_control);
		linear_control_b0 = (LinearLayout) findViewById(R.id.linear_control_b0);
		linear_control_b1 = (LinearLayout) findViewById(R.id.linear_control_b1);
		linear_control_b2 = (LinearLayout) findViewById(R.id.linear_control_b2);
		linear_control_b7 = (LinearLayout) findViewById(R.id.linear_control_b7);
		linear_control_b3 = (LinearLayout) findViewById(R.id.linear_control_b3);
		linear_control_b4 = (LinearLayout) findViewById(R.id.linear_control_b4);
		linear_control_b5 = (LinearLayout) findViewById(R.id.linear_control_b5);
		linear_control_b6 = (LinearLayout) findViewById(R.id.linear_control_b6);
		linear_control_b9 = (LinearLayout) findViewById(R.id.linear_control_b9);
		linear_control_b10 = (LinearLayout) findViewById(R.id.linear_control_b10);
		linear_control_b8 = (LinearLayout) findViewById(R.id.linear_control_b8);
		linear_control_endp = (LinearLayout) findViewById(R.id.linear_control_endp);
		back = (ImageView) findViewById(R.id.back);
		forward = (ImageView) findViewById(R.id.forward);
		reload = (ImageView) findViewById(R.id.reload);
		homepage_ic = (ImageView) findViewById(R.id.homepage_ic);
		desktop_switch = (ImageView) findViewById(R.id.desktop_switch);
		ic_clear = (ImageView) findViewById(R.id.ic_clear);
		ic_share = (ImageView) findViewById(R.id.ic_share);
		settings = (ImageView) findViewById(R.id.settings);
		history = (ImageView) findViewById(R.id.history);
		imageview1 = (ImageView) findViewById(R.id.imageview1);
		exit = (ImageView) findViewById(R.id.exit);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dhist = new AlertDialog.Builder(this);
		bookmarks = getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.win98_error);
		
		browse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (urledit.getText().toString().equals("browservio://no_error")) {
					throw new RuntimeException("Resource access denied, reason: 'browservio://no_error is a protected webpage'");
				}
				else {
					if (urledit.getText().toString().equals("browservio://error") || urledit.getText().toString().equals("file:///android_asset/error.html")) {
						_errorpage();
					}
					else {
						if (browservio_saver.getString("overrideEmptyError", "").equals("1") && urledit.getText().toString().equals("")) {
							_browservio_browse();
						}
						else {
							if (urledit.getText().toString().equals("")) {
								urledit.setError("This flied cannot be empty");
								error_defuse = new TimerTask() {
									@Override
									public void run() {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												urledit.setError(null);
											}
										});
									}
								};
								_timer.schedule(error_defuse, (int)(3000));
							}
							else {
								_browservio_browse();
							}
						}
					}
				}
				if (!browservio_saver.getString("history", "").equals("")) {
					browservio_saver.edit().putString("history", browservio_saver.getString("history", "").concat("\n".concat(webview.getUrl()))).apply();
				}
			}
		});
		
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				
				super.onPageFinished(_param1, _param2);
			}
		});
		
		linear_control_b0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// on forward being clicked, either go forward in history
				if (webview.canGoBack()) {
					// can go back
					webview.goBack();
				}
				else {
					// cannot go backwards
					SketchwareUtil.showMessage(getApplicationContext(), "Already at the first page!");
				}
			}
		});
		
		linear_control_b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// on forward being clicked, either go forward in history
				if (webview.canGoForward()) {
					// can go forward
					webview.goForward();
				}
				else {
					// cannot go forward
					SketchwareUtil.showMessage(getApplicationContext(), "Already at the last page!");
				}
			}
		});
		
		linear_control_b2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (page_before_error.equals("browservio://no_error")) {
					if (!webview.getUrl().equals("")) {
						webview.reload();
					}
				}
				else {
					webview.loadUrl(page_before_error);
					urledit.setText(page_before_error);
					page_before_error = "browservio://no_error";
				}
			}
		});
		
		linear_control_b7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				webview.loadUrl(browservio_saver.getString("defaultHomePage", ""));
			}
		});
		
		linear_control_b3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				PopupMenu popup1 = new PopupMenu(MainActivity.this, linear_control_b3);
				Menu menu1 = popup1.getMenu();
				menu1.add("Desktop");
				menu1.add("Mobile");
				menu1.add("Custom");
				popup1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item){
						switch (item.getTitle().toString()){
							case "Desktop":
							webview.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36 Browservio/1.4.0_beroku_dev_4");
							last_desktop = desktop;
							desktop = 1;
							desktop_switch.setImageResource(R.drawable.ic_desktop_black);
							linear_control_b2.performClick();
							break;
							case "Mobile":
							webview.getSettings().setUserAgentString(System.getProperty("http.agent").toString());
							last_desktop = desktop;
							desktop = 0;
							desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
							linear_control_b2.performClick();
							break;
							case "Custom":
							dialog.setTitle("User agent");
							dialog.setMessage("Please set the custom user agent of your choosing:");
							final EditText custom_ua = new EditText(MainActivity.this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); 
							custom_ua.setLayoutParams(lp); dialog.setView(custom_ua);
							dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface _dialog, int _which) {
									if (custom_ua.length() == 0) {
										webview.getSettings().setUserAgentString(System.getProperty("http.agent").toString());
										linear_control_b2.performClick();
										desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
										desktop = 0;
									} else {
										webview.getSettings().setUserAgentString(custom_ua.getText().toString());
										linear_control_b2.performClick();
									}
								}
							});
							dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface _dialog, int _which) {
									if (last_desktop == 0) {
										desktop = last_desktop;
										desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
									}
									else {
										if (last_desktop == 1) {
											desktop = last_desktop;
											desktop_switch.setImageResource(R.drawable.ic_desktop_black);
										}
										else {
											if (last_desktop == 2) {
												desktop = last_desktop;
												desktop_switch.setImageResource(R.drawable.ic_edit_black);
											}
											else {
												throw new RuntimeException("last_desktop out of possible range 0-2");
											}
										}
									}
								}
							});
							dialog.setCancelable(false);
							dialog.create().show();
							last_desktop = desktop;
							desktop = 2;
							desktop_switch.setImageResource(R.drawable.ic_edit_black);
							break;}
						return true;
					}
				});
				popup1.show();
			}
		});
		
		linear_control_b4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				PopupMenu popup2 = new PopupMenu(MainActivity.this, linear_control_b3);
				Menu menu2 = popup2.getMenu();
				menu2.add("Clear Cache");
				menu2.add("Clear History");
				menu2.add("Clear Cookies");
				menu2.add("Clear All");
				popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item){
						switch (item.getTitle().toString()){
							case "Clear Cache":
							webview.clearCache(true);
							SketchwareUtil.showMessage(getApplicationContext(), "Cache cleared successfully!");
							linear_control_b2.performClick();
							break;
							case "Clear History":
							webview.clearHistory();
							browservio_saver.edit().putString("history", "").apply();
							SketchwareUtil.showMessage(getApplicationContext(), "History cleared successfully!");
							linear_control_b2.performClick();
							break;
							case "Clear Cookies":
							CookieManager.getInstance().removeAllCookies(null);
							            CookieManager.getInstance().flush();
							linear_control_b2.performClick();
							break;
							case "Clear All":
							webview.clearCache(true);
							webview.clearHistory();
							browservio_saver.edit().putString("history", "").apply();
							CookieManager.getInstance().removeAllCookies(null);
							            CookieManager.getInstance().flush();
							SketchwareUtil.showMessage(getApplicationContext(), "Everything cleared successfully!");
							linear_control_b2.performClick();
							break;}
						return true;
					}
				});
				popup2.show();
			}
		});
		
		linear_control_b5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent i = new Intent(android.content.Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(android.content.Intent.EXTRA_TEXT, webview.getUrl());
				startActivity(Intent.createChooser(i,"Share URL using"));
			}
		});
		
		linear_control_b6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				i.setClass(getApplicationContext(), SettingsActivity.class);
				startActivity(i);
			}
		});
		
		linear_control_b9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (!browservio_saver.getString("history", "").equals("")) {
					dhist.setTitle("History");
					dhist.setMessage(browservio_saver.getString("history", ""));
					dhist.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {
							
						}
					});
					dhist.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {
							((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", browservio_saver.getString("history", "")));
							SketchwareUtil.showMessage(getApplicationContext(), "Copied to clipboard!");
						}
					});
					dhist.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {
							browservio_saver.edit().putString("history", "").apply();
							SketchwareUtil.showMessage(getApplicationContext(), "Cleared successfully!");
						}
					});
					dhist.create().show();
				}
				else {
					SketchwareUtil.showMessage(getApplicationContext(), "History is empty!");
				}
			}
		});
		
		linear_control_b10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				PopupMenu popup3 = new PopupMenu(MainActivity.this, linear_control_b3);
				Menu menu3 = popup3.getMenu();
				menu3.add("Add...");
				menu3.add("Favourites");
				popup3.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item){
						switch (item.getTitle().toString()){
							case "Add...":
							if (bookmarks.getString("bookmarked_count", "").equals("")) {
								bookmarks.edit().putString("bookmarked_count", "0").apply();
							}
							else {
								bookmarks.edit().putString("bookmarked_count", String.valueOf((long)(Double.parseDouble(bookmarks.getString("bookmarked_count", "")) + 1))).apply();
							}
							bookmarks.edit().putString("bookmark_".concat(bookmarks.getString("bookmarked_count", "")), webview.getUrl()).apply();
							bookmarks.edit().putString("bookmark_".concat(bookmarks.getString("bookmarked_count", "")).concat("_show"), "1").apply();
							SketchwareUtil.showMessage(getApplicationContext(), "Saved successfully!");
							break;
							case "Favourites":
							i.setClass(getApplicationContext(), FavActivity.class);
							startActivity(i);
							break;}
						return true;
					}
				});
				popup3.show();
			}
		});
		
		linear_control_b8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});
		
		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				baranim.setTarget(_fab);
				barrrrrr.setTarget(hscroll_control);
				baranim.setPropertyName("rotation");
				barrrrrr.setPropertyName("alpha");
				baranim.setDuration((int)(250));
				barrrrrr.setDuration((int)(250));
				if (linear_control.getVisibility() == View.VISIBLE) {
					baranim.setFloatValues((float)(180), (float)(0));
					barrrrrr.setFloatValues((float)(1), (float)(0));
					linear_control.setVisibility(View.GONE);
				} else {
					linear_control.setVisibility(View.VISIBLE);
					baranim.setFloatValues((float)(0), (float)(180));
					barrrrrr.setFloatValues((float)(0), (float)(1));
				}
				baranim.start();
				barrrrrr.start();
			}
		});
	}
	private void initializeLogic() {
		// Welcome to the Browservio (Shrek without Shrek browser)
		// This browser was originally designed with Sketchware
		// This project was started on Aug 13 2020
		// sur wen Sherk browser
		setTitle("Browservio");
		webview.setWebChromeClient(new CustomWebClient());
		// Keyboard press = browse
		urledit.setOnEditorActionListener(new EditText.OnEditorActionListener() { 
			  public boolean
			  onEditorAction(TextView v, int actionId, KeyEvent event) { 
				    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
					        browse.performClick(); 
					        return true; 
					    } 
				    return false; 
				  } 
		});
		// Page stuff
		page_before_error = "browservio://no_error";
		// desktopMode init code
		webview.getSettings().setUserAgentString(System.getProperty("http.agent").toString());
		desktop = 0;
		last_desktop = desktop;
		// Start downloadManager service
		_downloadManager(webview);
		// Set default fab stat
		linear_control.setVisibility(View.GONE);
		// Custom error page
		webview.setWebViewClient(new WebViewClient() {
			
			    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (!defaulterror) {
					page_before_error = urledit.getText().toString();
					_errorpage();
				}
				
				    }
		});
		if (!browservio_saver.getString("defaultHomePage", "").equals("")) {
			// Load default homepage.
			if (browservio_saver.getString("defaultHomePage", "").contains("browservio://no_error")) {
				browservio_saver.edit().putString("defaultHomePage", "https://www.google.com/").apply();
			}
			webview.loadUrl(browservio_saver.getString("defaultHomePage", ""));
			_URLindentify(1);
		}
		else {
			browservio_saver.edit().putString("defaultHomePage", "https://www.google.com/").apply();
		}
		// zoom stuff - From SCMPNews
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDisplayZoomControls(false);
	}
	
	public class CustomWebClient extends WebChromeClient {
		private View mCustomView;
		private WebChromeClient.CustomViewCallback mCustomViewCallback;
		protected FrameLayout frame;
		
		// Initially mOriginalOrientation is set to Landscape
		private int mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		private int mOriginalSystemUiVisibility;
		
		// Constructor for CustomWebClient
		public CustomWebClient() {}
		
		public Bitmap getDefaultVideoPoster() {
			if (MainActivity.this == null) {
				return null; }
			return BitmapFactory.decodeResource(MainActivity.this.getApplicationContext().getResources(), 2130837573); }
		
		public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback viewCallback) {
			if (this.mCustomView != null) {
				onHideCustomView();
				return; }
			this.mCustomView = paramView;
			this.mOriginalSystemUiVisibility = MainActivity.this.getWindow().getDecorView().getSystemUiVisibility();
			// When CustomView is shown screen orientation changes to mOriginalOrientation (Landscape).
			MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
			// After that mOriginalOrientation is set to portrait.
			this.mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			this.mCustomViewCallback = viewCallback; ((FrameLayout)MainActivity.this.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1)); MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(3846);
		}
		
		public void onHideCustomView() {
			((FrameLayout)MainActivity.this.getWindow().getDecorView()).removeView(this.mCustomView);
			this.mCustomView = null;
			MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
			// When CustomView is hidden, screen orientation is set to mOriginalOrientation (portrait).
			MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
			// After that mOriginalOrientation is set to landscape.
			this.mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; this.mCustomViewCallback.onCustomViewHidden();
			this.mCustomViewCallback = null;
		}
	}
	
	{
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			
			default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		// onBackPressed to go back in history or finish activity
		if (webview.canGoBack()) {
			// Go back
			webview.goBack();
		}
		else {
			// Finish activity
			finish();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		funload.cancel();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		_firstLaunch();
		funload = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						finload = webview.getProgress();
						if (finload == 100) {
							progmain.setProgress((int)0);
							pooran = true;
						}
						else {
							progmain.setProgress((int)finload);
							CookieSyncManager.getInstance().sync();
							_URLindentify(1);
						}
					}
				});
			}
		};
		_timer.scheduleAtFixedRate(funload, (int)(0), (int)(70));
	}
	private void _downloadManager (final WebView _webview) {
		_webview.setDownloadListener(new DownloadListener() {       
			    @Override
			    public void onDownloadStart(String url, String userAgent,
			                                    String contentDisposition, String mimetype,
			                                    long contentLength) {
				            DownloadManager.Request request = new DownloadManager.Request(
				                    Uri.parse(url));
				
				            request.allowScanningByMediaScanner();
				            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
				            final String filename= URLUtil.guessFileName(url, contentDisposition, mimetype);
				            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
				            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				            dm.enqueue(request);
				SketchwareUtil.showMessage(getApplicationContext(), "Downloading file...");
				        }
			    });
	}

	private void _browservio_browse () {
		if (page_before_error.equals("browservio://no_error")) {
			// Load URL from editurl
			if(URLUtil.isValidUrl(urledit.getText().toString())) {
				webview.loadUrl(urledit.getText().toString());
			} else {
				googleLoad = browservio_saver.getString("defaultSearch", "").concat(urledit.getText().toString());
				webview.loadUrl(googleLoad);
				urledit.setText(googleLoad);
			}
		}
		else {
			webview.loadUrl(page_before_error);
			urledit.setText(page_before_error);
			page_before_error = "browservio://no_error";
		}
	}
	
	
	private void _firstLaunch () {
		// First launch code
		// Make versionName and versionTech. using version family
		// intro. 20201027 with 1.4.0_beroku_dev_8
		browservio_saver.edit().putString("versionFamily", "1.4.0").apply();
		browservio_saver.edit().putString("versionName", browservio_saver.getString("versionFamily", "").concat(" Dev 20201027a")).apply();
		browservio_saver.edit().putString("versionTechnical", browservio_saver.getString("versionFamily", "").concat("_beroku_dev_8")).apply();
		browservio_saver.edit().putString("versionCodename", "Beroku").apply();
		browservio_saver.edit().putString("versionCode", "26").apply();
		browservio_saver.edit().putString("versionDate", "2020-10-27").apply();
		if (!browservio_saver.getString("configVersion", "").equals("9") && !browservio_saver.getString("configVersion", "").equals("")) {
			dialog.setTitle("Your settings has been reset!");
			dialog.setMessage("To ensure stability, we've reset your settings to default because you've just installed an update.");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					
				}
			});
			dialog.create().show();
		}
		if (browservio_saver.getString("isFirstLaunch", "").equals("1")) {
			final ProgressDialog prog = new ProgressDialog(MainActivity.this);
			prog.setMax(100);
			prog.setTitle("Reset");
			prog.setMessage("Reseting...");
			prog.setIndeterminate(true);
			prog.setCancelable(false);
			prog.show();
			browservio_saver.edit().putString("isFirstLaunch", "").apply();
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
			browservio_saver.edit().putString("history", "").apply();
			webview.clearCache(true);
			webview.clearHistory();
			reset = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Intent i = getIntent();
							finish();
							startActivity(i);
							SketchwareUtil.showMessage(getApplicationContext(), "Reset successfully!");
							reset.cancel();
						}
					});
				}
			};
			_timer.schedule(reset, (int)(2000));
		}
		if (browservio_saver.getString("defaultHomePage", "").equals("")) {
			Intent i = getIntent();
			finish();
			startActivity(i);
		}
		if (!browservio_saver.getString("configVersion", "").equals("9") || (browservio_saver.getString("isFirstLaunch", "").equals("") || browservio_saver.getString("isFirstLaunch", "").equals("1"))) {
			browservio_saver.edit().putString("isJavaScriptEnabled", "1").apply();
			browservio_saver.edit().putString("defaultHomePage", "https://www.google.com/").apply();
			browservio_saver.edit().putString("defaultSearch", "https://www.google.com/search?q=").apply();
			browservio_saver.edit().putString("overrideEmptyError", "0").apply();
			browservio_saver.edit().putString("showBrowseBtn", "0").apply();
			browservio_saver.edit().putString("showZoomKeys", "0").apply();
			browservio_saver.edit().putString("showCustomError", "1").apply();
			browservio_saver.edit().putString("configVersion", "9").apply();
			browservio_saver.edit().putString("isFirstLaunch", "0").apply();
		}
		// Settings check
		if (browservio_saver.getString("isJavaScriptEnabled", "").equals("1")) {
			webview.getSettings().setJavaScriptEnabled(true);
			webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		}
		else {
			webview.getSettings().setJavaScriptEnabled(false);
			webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		}
		if (browservio_saver.getString("showBrowseBtn", "").equals("1")) {
			browse.setVisibility(View.VISIBLE);
		}
		else {
			browse.setVisibility(View.GONE);
		}
		if (browservio_saver.getString("showCustomError", "").equals("1")) {
			defaulterror = false;
		}
		else {
			defaulterror = true;
		}
		if (browservio_saver.getString("showZoomKeys", "").equals("1")) {
			webview.getSettings().setDisplayZoomControls(true);
		}
		else {
			webview.getSettings().setDisplayZoomControls(false);
		}
		browservio_saver.edit().putString("lastConfigVersion", browservio_saver.getString("configVersion", "")).apply();
		browservio_saver.edit().putString("lastVersionCode", browservio_saver.getString("versionCode", "")).apply();
	}

	private void playSong(int position) {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer = MediaPlayer.create(getApplicationContext(), resID[position]);
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {

				if (timesPosition<times){
					if (songPosition <= to){
						songPosition = songPosition + 1;
					} else {
						songPosition = from;
						songPosition = timesPosition + timesPosition + 1;
					}
					playSong(songPosition);
				}
			}
		});
	}

	private void _errorpage () {
		webview.loadUrl("file:///android_asset/error.html");
		//Setup media player (rewrote 200815-1307)
		playSong(songPosition);
		urledit.setText("browservio://error");
	}
	
	
	private void _URLindentify (final double _type) {
		if (_type == 1) {
			if (webview.getUrl().equals("browservio://error") || webview.getUrl().equals("file:///android_asset/error.html")) {
				_errorpage();
			}
			else {
				urledit.setText(webview.getUrl());
			}
		}
		else {
			if (urledit.getText().toString().equals("browservio://error") || urledit.getText().toString().equals("file://".concat(FileUtil.getExternalStorageDir().concat("/Browservio/error/error.html")))) {
				_errorpage();
			}
			else {
				urledit.setText(webview.getUrl());
			}
		}
		if (pooran) {
			browservio_saver.edit().putString("history", browservio_saver.getString("history", "").concat("\n".concat(webview.getUrl()))).apply();
			pooran = false;
		}
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels(){
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels(){
		return getResources().getDisplayMetrics().heightPixels;
	}
	
}
