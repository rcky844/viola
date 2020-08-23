package tipz.browservio;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.text.*;
import android.util.*;
import android.webkit.*;
import android.animation.*;
import android.view.animation.*;
import java.util.*;
import java.text.*;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.EditText;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.HorizontalScrollView;
import android.app.Activity;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.media.MediaPlayer;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import java.util.Timer;
import java.util.TimerTask;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private FloatingActionButton _fab;
	private double fabstat = 0;
	private double desktop = 0;
	private double last_desktop = 0;
	private String page_before_error = "";
	private String googleLoad = "";
	private boolean defaulterror = false;
	private String beforepauseUrl = "";
	private boolean actuallypaused = false;
	private boolean actuallypressedbro = false;
	
	private LinearLayout linear_urledit;
	private LinearLayout webview_linear;
	private LinearLayout linear_urledit_text;
	private ImageView browse;
	private EditText urledit;
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
	private LinearLayout linear_control_b8;
	private LinearLayout linear5;
	private ImageView back;
	private ImageView forward;
	private ImageView reload;
	private ImageView homepage_ic;
	private ImageView desktop_switch;
	private ImageView ic_clear;
	private ImageView ic_share;
	private ImageView settings;
	private ImageView imageview1;
	
	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private Intent i = new Intent();
	private MediaPlayer errorsound;
	private ObjectAnimator baranim = new ObjectAnimator();
	private TimerTask reloadt;
	private TimerTask error_defuse;
	private TimerTask starthome;
	private TimerTask reloadset;
	private TimerTask crrurl;
	private TimerTask reset;
	private ObjectAnimator barrrrrr = new ObjectAnimator();
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
		linear_control_b8 = (LinearLayout) findViewById(R.id.linear_control_b8);
		linear5 = (LinearLayout) findViewById(R.id.linear5);
		back = (ImageView) findViewById(R.id.back);
		forward = (ImageView) findViewById(R.id.forward);
		reload = (ImageView) findViewById(R.id.reload);
		homepage_ic = (ImageView) findViewById(R.id.homepage_ic);
		desktop_switch = (ImageView) findViewById(R.id.desktop_switch);
		ic_clear = (ImageView) findViewById(R.id.ic_clear);
		ic_share = (ImageView) findViewById(R.id.ic_share);
		settings = (ImageView) findViewById(R.id.settings);
		imageview1 = (ImageView) findViewById(R.id.imageview1);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		
		browse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (!actuallypressedbro) {
					_rippleAnimator("grey", browse);
					actuallypressedbro = true;
				}
				if (urledit.getText().toString().equals("browservio://defaulterror") || urledit.getText().toString().equals("file:///sdcard/Browservio/error/setorerr.html")) {
					defaulterror = true;
					urledit.setText("browservio://defaulterror");
					webview.loadUrl("file:///sdcard/Browservio/error/setorerr.html");
				}
				else {
					if (urledit.getText().toString().equals("browservio://error") || urledit.getText().toString().equals("file:///sdcard/Browservio/error/error.html")) {
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
				_rippleAnimator("grey", linear_control_b0);
				// on forward being clicked, either go forward in history
				if (webview.canGoBack()) {
					// can go back
					webview.goBack();
					// Fix URL not showing correctly
					crrurl = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									_URLindentify(2);
								}
							});
						}
					};
					_timer.schedule(crrurl, (int)(250));
				}
				else {
					// cannot go backwards
					SketchwareUtil.showMessage(getApplicationContext(), "Already at the first page");
				}
			}
		});
		
		linear_control_b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear_control_b1);
				// on forward being clicked, either go forward in history
				if (webview.canGoForward()) {
					// can go forward
					webview.goForward();
					// Fix URL not showing correctly
					crrurl = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									_URLindentify(2);
								}
							});
						}
					};
					_timer.schedule(crrurl, (int)(250));
				}
				else {
					// cannot go forward
					SketchwareUtil.showMessage(getApplicationContext(), "Already at the last page");
				}
			}
		});
		
		linear_control_b2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear_control_b2);
				if (page_before_error.equals("browservio://no_error")) {
					if (!webview.getUrl().equals("")) {
						webview.reload();
						reloadt = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										_URLindentify(0);
									}
								});
							}
						};
						_timer.schedule(reloadt, (int)(250));
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
				_rippleAnimator("grey", linear_control_b7);
				webview.loadUrl(browservio_saver.getString("defaultHomePage", ""));
				reloadset = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								_URLindentify(0);
							}
						});
					}
				};
				_timer.schedule(reloadset, (int)(250));
			}
		});
		
		linear_control_b3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear_control_b3);
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
							webview.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
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
				_rippleAnimator("grey", linear_control_b4);
				PopupMenu popup2 = new PopupMenu(MainActivity.this, linear_control_b3);
				Menu menu2 = popup2.getMenu();
				menu2.add("Clear Cache");
				menu2.add("Clear History");
				menu2.add("Clear All");
				popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item){
						switch (item.getTitle().toString()){
							case "Clear Cache":
							webview.clearCache(true);
							SketchwareUtil.showMessage(getApplicationContext(), "Cleared successfully!");
							linear_control_b2.performClick();
							break;
							case "Clear History":
							webview.clearHistory();
							SketchwareUtil.showMessage(getApplicationContext(), "Cleared successfully!");
							linear_control_b2.performClick();
							break;
							case "Clear All":
							webview.clearCache(true);
							webview.clearHistory();
							SketchwareUtil.showMessage(getApplicationContext(), "Cleared successfully!");
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
				_rippleAnimator("grey", linear_control_b5);
				Intent i = new Intent(android.content.Intent.ACTION_SEND); i.setType("text/plain");  i.putExtra(android.content.Intent.EXTRA_TEXT, webview.getUrl()); startActivity(Intent.createChooser(i,"Share URL using"));
				// Show the bottom bar how it was before pausing
				_bottomBarchk();
			}
		});
		
		linear_control_b6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear_control_b6);
				i.setClass(getApplicationContext(), SettingsActivity.class);
				startActivity(i);
			}
		});
		
		linear_control_b8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear_control_b8);
				finish();
			}
		});
		
		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (fabstat == 0) {
					fabstat = 1;
					linear_control.setVisibility(View.VISIBLE);
					baranim.setTarget(_fab);
					barrrrrr.setTarget(hscroll_control);
					baranim.setPropertyName("rotation");
					barrrrrr.setPropertyName("alpha");
					baranim.setFloatValues((float)(0), (float)(180));
					barrrrrr.setFloatValues((float)(0), (float)(1));
					baranim.setDuration((int)(250));
					barrrrrr.setDuration((int)(250));
					baranim.start();
					barrrrrr.start();
				}
				else {
					fabstat = 0;
					baranim.setTarget(_fab);
					barrrrrr.setTarget(hscroll_control);
					baranim.setPropertyName("rotation");
					barrrrrr.setPropertyName("alpha");
					baranim.setFloatValues((float)(180), (float)(0));
					barrrrrr.setFloatValues((float)(1), (float)(0));
					baranim.setDuration((int)(250));
					barrrrrr.setDuration((int)(250));
					baranim.start();
					barrrrrr.start();
					linear_control.setVisibility(View.GONE);
				}
			}
		});
	}
	private void initializeLogic() {
		setTitle("Browservio");
		_firstLaunch();
		actuallypaused = false;
		// Keyboard press = browse
		urledit.setOnEditorActionListener(new EditText.OnEditorActionListener() { 
			  public boolean
			  onEditorAction(TextView v, int actionId, KeyEvent event) { 
				    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
					      browse.performClick(); 
					      
					actuallypressedbro = false;
					       return true; 
					    } 
				    return false; 
				  } 
		});
		// full screen vid
		_fullScreenVideo();
		webview.setWebChromeClient(new CustomWebClient());
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
		if (actuallypaused) {
			
		}
		else {
			// onBackPressed to go back in history or finish activity
			if (webview.canGoBack()) {
				// Go back
				webview.goBack();
				// Fix URL not showing correctly
				crrurl = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (urledit.getText().toString().equals("file:///sdcard/Browservio/error/setorerr.html")) {
									urledit.setText("browservio://defaulterror");
								}
								else {
									if (webview.getUrl().equals("file:///sdcard/Browservio/error/error.html")) {
										urledit.setText("browservio://error");
									}
									else {
										urledit.setText(webview.getUrl());
									}
								}
							}
						});
					}
				};
				_timer.schedule(crrurl, (int)(250));
			}
			else {
				// Finish activity
				finish();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		FileUtil.deleteFile(FileUtil.getExternalStorageDir().concat("/Browservio/error"));
	}
	
	@Override
	public void onStart() {
		super.onStart();
		page_before_error = "browservio://no_error";
		// desktopMode init code
		webview.getSettings().setUserAgentString(System.getProperty("http.agent").toString());
		desktop = 0;
		last_desktop = 0;
		desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
		// Start downloadManager service
		_downloadManager(webview);
		// Set default pic for fab 
		_fab.setImageResource(R.drawable.ic_arrow_up_white);
		// Set default fab stat
		linear_control.setVisibility(View.GONE);
		fabstat = 0;
		// Custom error page
		FileUtil.writeFile(FileUtil.getExternalStorageDir().concat("/Browservio/error/error.html"), "<!DOCTYPE html> <html> <head> <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, viewport-fit=cover\"> <title>Error!</title> <style type=\"text/css\">@font-face{font-family:FSEX300;src:url(data:application/x-font-ttf;charset=utf-8;base64,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A3oDpgO2A8oD4gP+BCYEPgReBHoEkgSuBMYE4gT6BRYFKgVCBVoFegWMBaIFtAXOBeYF/gYUBjAGSAZaBnIGjgayBtAG8AAAAAEAAABEACQAAwAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAUAPYAAQAAAAAAAQAXAAAAAQAAAAAAAgAHABcAAQAAAAAAAwAuAB4AAQAAAAAABAAXAEwAAQAAAAAABQASAGMAAQAAAAAABgAVAHUAAQAAAAAACAAQAIoAAQAAAAAACQAQAJoAAQAAAAAACwAhAKoAAQAAAAAADAAhAMsAAwABBAkAAQAuAOwAAwABBAkAAgAOARoAAwABBAkAAwBcASgAAwABBAkABAAuAYQAAwABBAkABQAkAbIAAwABBAkABgAqAdYAAwABBAkACAAgAgAAAwABBAkACQAgAiAAAwABBAkACwBCAkAAAwABBAkADABCAoJGaXhlZHN5cyBFeGNlbHNpb3IgMy4wMVJlZ3VsYXJEYXJpZW5WYWxlbnRpbmU6IEZpeGVkc3lzIEV4Y2Vsc2lvciAzLjAxOiAyMDA3Rml4ZWRzeXMgRXhjZWxzaW9yIDMuMDFWZXJzaW9uIDMuMDEwIDIwMDdGaXhlZHN5c0V4Y2Vsc2lvcklJSWJEYXJpZW4gVmFsZW50aW5lRGFyaWVuIFZhbGVudGluZWh0dHA6Ly93d3cuZml4ZWRzeXNleGNlbHNpb3IuY29tL2h0dHA6Ly93d3cuZml4ZWRzeXNleGNlbHNpb3IuY29tLwBGAGkAeABlAGQAcwB5AHMAIABFAHgAYwBlAGwAcwBpAG8AcgAgADMALgAwADEAUgBlAGcAdQBsAGEAcgBEAGEAcgBpAGUAbgBWAGEAbABlAG4AdABpAG4AZQA6ACAARgBpAHgAZQBkAHMAeQBzACAARQB4AGMAZQBsAHMAaQBvAHIAIAAzAC4AMAAxADoAIAAyADAAMAA3AEYAaQB4AGUAZABzAHkAcwAgAEUAeABjAGUAbABzAGkAbwByACAAMwAuADAAMQBWAGUAcgBzAGkAbwBuACAAMwAuADAAMQAwACAAMgAwADAANwBGAGkAeABlAGQAcwB5AHMARQB4AGMAZQBsAHMAaQBvAHIASQBJAEkAYgBEAGEAcgBpAGUAbgAgAFYAYQBsAGUAbgB0AGkAbgBlAEQAYQByAGkAZQBuACAAVgBhAGwAZQBuAHQAaQBuAGUAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAGYAaQB4AGUAZABzAHkAcwBlAHgAYwBlAGwAcwBpAG8AcgAuAGMAbwBtAC8AaAB0AHQAcAA6AC8ALwB3AHcAdwAuAGYAaQB4AGUAZABzAHkAcwBlAHgAYwBlAGwAcwBpAG8AcgAuAGMAbwBtAC8AAAACAAAAAAAA//EACgAAAAAAAAAAAAAAAAAAAAAAAABEAEQAAAAEAAsADAAPABEAEwAUABUAFgAXABgAGQAaABsAHAAkACUAJgAnACgAKQAqACsALAAtAC4ALwAwADEAMgAzADQANQA2ADcAOAA5ADoAOwA8AD0ARABFAEYARwBIAEkASgBLAEwATQBOAE8AUABRAFIAUwBUAFUAVgBXAFgAWQBaAFsAXABdAAA=) format(\"truetype\"),url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxmb250IGlkPSJmb250ZWRpdG9yIiBob3Jpei1hZHYteD0iODAiPjxmb250LWZhY2UgZm9udC1mYW1pbHk9IkZpeGVkc3lzIEV4Y2Vsc2lvciAzLjAxIiBmb250LXdlaWdodD0iNDAwIiB1bml0cy1wZXItZW09IjE2MCIgcGFub3NlLTE9IjIgMTEgNiAwIDcgNyAyIDQgMiA0IiBhc2NlbnQ9IjEzMCIgZGVzY2VudD0iLTMwIiB4LWhlaWdodD0iNCIgYmJveD0iMCAtMzAgODAgMTAwIiB1bmRlcmxpbmUtdGhpY2tuZXNzPSIxMCIgdW5kZXJsaW5lLXBvc2l0aW9uPSItMTUiIHVuaWNvZGUtcmFuZ2U9IlUrMDAyMS0wMDdhIi8+PGdseXBoIGdseXBoLW5hbWU9ImV4Y2xhbSIgdW5pY29kZT0iISIgZD0iTTYwIDUwSDUwVjMwSDMwdjIwSDIwdjMwaDEwdjEwaDIwVjgwaDEwVjUwek01MCAwSDMwdjIwaDIwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9InBhcmVubGVmdCIgdW5pY29kZT0iKCIgZD0iTTYwLTIwSDQwdjEwSDMwdjIwSDIwdjUwaDEwdjIwaDEwdjEwaDIwVjgwSDUwVjYwSDQwVjEwaDEwdi0yMGgxMHYtMTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9InBhcmVucmlnaHQiIHVuaWNvZGU9IikiIGQ9Ik02MCAxMEg1MHYtMjBINDB2LTEwSDIwdjEwaDEwdjIwaDEwdjUwSDMwdjIwSDIwdjEwaDIwVjgwaDEwVjYwaDEwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJjb21tYSIgdW5pY29kZT0iLCIgZD0iTTYwLTEwSDUwdi0xMEgzMHYxMGgxMFYwSDMwdjIwaDMwdi0zMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0icGVyaW9kIiB1bmljb2RlPSIuIiBkPSJNNjAgMEgzMHYyMGgzMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ6ZXJvIiB1bmljb2RlPSIwIiBkPSJNODAgMTBINzBWMEgzMHYxMEgyMHY3MGgxMHYxMGg0MFY4MGgxMFYxMHptLTIwIDB2NDBINTB2MjBoMTB2MTBINDBWNDBoMTBWMjBINDBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9Im9uZSIgdW5pY29kZT0iMSIgZD0iTTYwIDBINDB2NjBIMTB2MTBoMjB2MTBoMTB2MTBoMjBWMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0idHdvIiB1bmljb2RlPSIyIiBkPSJNNzAgMEgxMHYyMGgxMHYxMGgxMHYxMGgxMHYxMGgxMHYzMEgzMFY2MEgxMHYyMGgxMHYxMGg0MFY4MGgxMFY1MEg2MFY0MEg1MFYzMEg0MFYyMEgzMFYxMGg0MFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ0aHJlZSIgdW5pY29kZT0iMyIgZD0iTTcwIDEwSDYwVjBIMjB2MTBIMTB2MjBoMjBWMTBoMjB2MzBIMzB2MTBoMjB2MzBIMzBWNjBIMTB2MjBoMTB2MTBoNDBWODBoMTBWNTBINjBWNDBoMTBWMTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9ImZvdXIiIHVuaWNvZGU9IjQiIGQ9Ik04MCAyMEg3MFYwSDUwdjIwSDEwdjIwaDEwdjUwaDIwVjQwSDMwVjMwaDIwdjQwaDIwVjMwaDEwVjIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJmaXZlIiB1bmljb2RlPSI1IiBkPSJNNzAgMjBINjBWMTBINTBWMEgxMHYxMGgzMHYxMGgxMHYyMEgxMHY1MGg2MFY4MEgzMFY1MGgzMFY0MGgxMFYyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0ic2l4IiB1bmljb2RlPSI2IiBkPSJNNzAgMTBINjBWMEgyMHYxMEgxMHY1MGgxMHYxMGgxMHYyMGgzMFY4MEg1MFY3MEg0MFY2MGgyMFY1MGgxMFYxMHptLTIwIDB2NDBIMzBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9InNldmVuIiB1bmljb2RlPSI3IiBkPSJNNzAgNzBINjBWNTBINTBWMzBINDBWMEgyMHYzMGgxMHYyMGgxMHYyMGgxMHYxMEgxMHYxMGg2MFY3MHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iZWlnaHQiIHVuaWNvZGU9IjgiIGQ9Ik03MCAxMEg2MFYwSDIwdjEwSDEwdjMwaDEwdjEwSDEwdjMwaDEwdjEwaDQwVjgwaDEwVjUwSDYwVjQwaDEwVjEwek01MCA1MHYzMEgzMFY2MGgxMFY1MGgxMHptMC00MHYyMEg0MHYxMEgzMFYxMGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0ibmluZSIgdW5pY29kZT0iOSIgZD0iTTcwIDMwSDYwVjIwSDUwVjBIMjB2MTBoMTB2MTBoMTB2MTBIMjB2MTBIMTB2NDBoMTB2MTBoNDBWODBoMTBWMzB6TTUwIDQwdjQwSDMwVjQwaDIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJBIiB1bmljb2RlPSJhIiBkPSJNNzAgMEg1MHYzMEgzMFYwSDEwdjcwaDEwdjEwaDEwdjEwaDIwVjgwaDEwVjcwaDEwVjB6TTUwIDQwdjMwSDMwVjQwaDIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJCIiB1bmljb2RlPSJiIiBkPSJNNzAgMTBINjBWMEgxMHY5MGg1MFY4MGgxMFY1MEg2MFY0MGgxMFYxMHpNNTAgNTB2MzBIMzBWNTBoMjB6bTAtNDB2MzBIMzBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkMiIHVuaWNvZGU9ImMiIGQ9Ik03MCAxMEg2MFYwSDIwdjEwSDEwdjcwaDEwdjEwaDQwVjgwaDEwVjYwSDUwdjIwSDMwVjEwaDIwdjIwaDIwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJEIiB1bmljb2RlPSJkIiBkPSJNNzAgMjBINjBWMTBINTBWMEgxMHY5MGg0MFY4MGgxMFY3MGgxMFYyMHptLTIwIDB2NTBINDB2MTBIMzBWMTBoMTB2MTBoMTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkUiIHVuaWNvZGU9ImUiIGQ9Ik03MCAwSDEwdjkwaDYwVjgwSDMwVjUwaDMwVjQwSDMwVjEwaDQwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkYiIHVuaWNvZGU9ImYiIGQ9Ik03MCA4MEgzMFY1MGgzMFY0MEgzMFYwSDEwdjkwaDYwVjgweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJHIiB1bmljb2RlPSJnIiBkPSJNNzAgMEgyMHYxMEgxMHY3MGgxMHYxMGg0MFY4MGgxMFY2MEg1MHYyMEgzMFYxMGgyMHYyMEg0MHYxMGgzMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJIIiB1bmljb2RlPSJoIiBkPSJNNzAgMEg1MHY0MEgzMFYwSDEwdjkwaDIwVjUwaDIwdjQwaDIwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkkiIHVuaWNvZGU9ImkiIGQ9Ik02MCAwSDIwdjEwaDEwdjcwSDIwdjEwaDQwVjgwSDUwVjEwaDEwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkoiIHVuaWNvZGU9ImoiIGQ9Ik03MCAxMEg2MFYwSDIwdjEwSDEwdjIwaDIwVjEwaDIwdjgwaDIwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJLIiB1bmljb2RlPSJrIiBkPSJNNzAgMEg1MHYyMEg0MHYyMEgzMFYwSDEwdjkwaDIwVjUwaDEwdjIwaDEwdjIwaDIwVjcwSDYwVjUwSDUwVjQwaDEwVjIwaDEwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IkwiIHVuaWNvZGU9ImwiIGQ9Ik03MCAwSDEwdjkwaDIwVjEwaDQwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9Ik0iIHVuaWNvZGU9Im0iIGQ9Ik04MCAwSDYwdjYwSDUwVjMwSDQwdjMwSDMwVjBIMTB2OTBoMjBWNzBoMTBWNjBoMTB2MTBoMTB2MjBoMjBWMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iTiIgdW5pY29kZT0ibiIgZD0iTTgwIDBINjB2MzBINTB2MTBINDB2MTBIMzBWMEgxMHY5MGgyMFY3MGgxMFY2MGgxMFY1MGgxMHY0MGgyMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJPIiB1bmljb2RlPSJvIiBkPSJNNzAgMTBINjBWMEgyMHYxMEgxMHY3MGgxMHYxMGg0MFY4MGgxMFYxMHptLTIwIDB2NzBIMzBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IlAiIHVuaWNvZGU9InAiIGQ9Ik03MCA1MEg2MFY0MEgzMFYwSDEwdjkwaDUwVjgwaDEwVjUwem0tMjAgMHYzMEgzMFY1MGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iUSIgdW5pY29kZT0icSIgZD0iTTcwLTIwSDUwdjEwSDQwVjBIMjB2MTBIMTB2NzBoMTB2MTBoNDBWODBoMTBWMTBINjB2LTIwaDEwdi0xMHpNNTAgMTB2NzBIMzBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IlIiIHVuaWNvZGU9InIiIGQ9Ik03MCAwSDUwdjMwSDQwdjEwSDMwVjBIMTB2OTBoNTBWODBoMTBWNTBINjBWMzBoMTBWMHpNNTAgNTB2MzBIMzBWNTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IlMiIHVuaWNvZGU9InMiIGQ9Ik03MCAxMEg2MFYwSDIwdjEwSDEwdjEwaDIwVjEwaDIwdjIwSDQwdjEwSDMwdjEwSDIwdjEwSDEwdjIwaDEwdjEwaDQwVjgwaDEwVjcwSDUwdjEwSDMwVjYwaDEwVjUwaDEwVjQwaDEwVjMwaDEwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJUIiB1bmljb2RlPSJ0IiBkPSJNNzAgODBINTBWMEgzMHY4MEgxMHYxMGg2MFY4MHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iVSIgdW5pY29kZT0idSIgZD0iTTcwIDEwSDYwVjBIMjB2MTBIMTB2ODBoMjBWMTBoMjB2ODBoMjBWMTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IlYiIHVuaWNvZGU9InYiIGQ9Ik03MCAyMEg2MFYxMEg1MFYwSDMwdjEwSDIwdjEwSDEwdjcwaDIwVjIwaDIwdjcwaDIwVjIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJXIiB1bmljb2RlPSJ3IiBkPSJNODAgMzBINzBWMEg1MHYzMEg0MFYwSDIwdjMwSDEwdjYwaDIwVjMwaDEwdjMwaDEwVjMwaDEwdjYwaDIwVjMweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJYIiB1bmljb2RlPSJ4IiBkPSJNNzAgMEg1MHYzMEg0MHYxMEgzMFYwSDEwdjMwaDEwdjEwaDEwdjIwSDIwdjEwSDEwdjIwaDIwVjcwaDEwVjYwaDEwdjMwaDIwVjcwSDYwVjYwSDUwVjQwaDEwVjMwaDEwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9IlkiIHVuaWNvZGU9InkiIGQ9Ik03MCA1MEg2MFY0MEg1MFYwSDMwdjQwSDIwdjEwSDEwdjQwaDIwVjUwaDIwdjQwaDIwVjUweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJaIiB1bmljb2RlPSJ6IiBkPSJNNzAgMEgxMHYzMGgxMHYxMGgxMHYxMGgxMHYxMGgxMHYyMEgxMHYxMGg2MFY2MEg2MFY1MEg1MFY0MEg0MFYzMEgzMFYxMGg0MFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJhIiB1bmljb2RlPSJhIiBkPSJNNzAgMEgyMHYxMEgxMHYyMGgxMHYxMGgzMHYyMEgyMHYxMGg0MFY2MGgxMFYwek01MCAxMHYyMEgzMFYxMGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iYiIgdW5pY29kZT0iYiIgZD0iTTcwIDEwSDYwVjBIMTB2OTBoMjBWNzBoMzBWNjBoMTBWMTB6bS0yMCAwdjUwSDMwVjEwaDIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJjIiB1bmljb2RlPSJjIiBkPSJNNzAgMTBINjBWMEgyMHYxMEgxMHY1MGgxMHYxMGg0MFY2MGgxMFY1MEg1MHYxMEgzMFYxMGgyMHYxMGgyMFYxMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iZCIgdW5pY29kZT0iZCIgZD0iTTcwIDBIMjB2MTBIMTB2NTBoMTB2MTBoMzB2MjBoMjBWMHpNNTAgMTB2NTBIMzBWMTBoMjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9ImUiIHVuaWNvZGU9ImUiIGQ9Ik03MCAzMEgzMFYxMGgzMFYwSDIwdjEwSDEwdjUwaDEwdjEwaDQwVjYwaDEwVjMwek01MCA0MHYyMEgzMFY0MGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iZiIgdW5pY29kZT0iZiIgZD0iTTcwIDQwSDQwVjBIMjB2NDBIMTB2MTBoMTB2MzBoMTB2MTBoNDBWODBINDBWNTBoMzBWNDB6Ii8+PGdseXBoIGdseXBoLW5hbWU9ImciIHVuaWNvZGU9ImciIGQ9Ik03MC0yMEg2MHYtMTBIMTB2MTBoNDBWMEgyMHYxMEgxMHY1MGgxMHYxMGg1MHYtOTB6TTUwIDEwdjUwSDMwVjEwaDIweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJoIiB1bmljb2RlPSJoIiBkPSJNNzAgMEg1MHY2MEgzMFYwSDEwdjkwaDIwVjcwaDMwVjYwaDEwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9ImkiIHVuaWNvZGU9ImkiIGQ9Ik01MCA4MEgzMHYyMGgyMFY4MHpNNzAgMEgxMHYxMGgyMHY1MEgxMHYxMGg0MFYxMGgyMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJqIiB1bmljb2RlPSJqIiBkPSJNNjAgODBINDB2MjBoMjBWODB6bTAtMTAwSDUwdi0xMEgxMHYxMGgzMHY4MEgyMHYxMGg0MHYtOTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9ImsiIHVuaWNvZGU9ImsiIGQ9Ik03MCAwSDUwdjIwSDQwdjEwSDMwVjBIMTB2OTBoMjBWNDBoMTB2MTBoMTB2MjBoMjBWNTBINjBWNDBINTBWMzBoMTBWMjBoMTBWMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0ibCIgdW5pY29kZT0ibCIgZD0iTTcwIDBIMTB2MTBoMjB2NzBIMTB2MTBoNDBWMTBoMjBWMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0ibSIgdW5pY29kZT0ibSIgZD0iTTgwIDBINjB2NjBINTBWMTBINDB2NTBIMzBWMEgxMHY3MGg2MFY2MGgxMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJuIiB1bmljb2RlPSJuIiBkPSJNNzAgMEg1MHY2MEgzMFYwSDEwdjcwaDUwVjYwaDEwVjB6Ii8+PGdseXBoIGdseXBoLW5hbWU9Im8iIHVuaWNvZGU9Im8iIGQ9Ik03MCAxMEg2MFYwSDIwdjEwSDEwdjUwaDEwdjEwaDQwVjYwaDEwVjEwem0tMjAgMHY1MEgzMFYxMGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0icCIgdW5pY29kZT0icCIgZD0iTTcwIDEwSDYwVjBIMzB2LTMwSDEwVjcwaDUwVjYwaDEwVjEwem0tMjAgMHY1MEgzMFYxMGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0icSIgdW5pY29kZT0icSIgZD0iTTcwLTMwSDUwVjBIMjB2MTBIMTB2NTBoMTB2MTBoNTBWLTMwek01MCAxMHY1MEgzMFYxMGgyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0iciIgdW5pY29kZT0iciIgZD0iTTcwIDUwSDQwVjQwSDMwVjBIMTB2NzBoMjBWNTBoMTB2MTBoMTB2MTBoMjBWNTB6Ii8+PGdseXBoIGdseXBoLW5hbWU9InMiIHVuaWNvZGU9InMiIGQ9Ik03MCAxMEg2MFYwSDEwdjEwaDQwdjIwSDIwdjEwSDEwdjIwaDEwdjEwaDUwVjYwSDMwVjQwaDMwVjMwaDEwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ0IiB1bmljb2RlPSJ0IiBkPSJNNzAgMEgzMHYxMEgyMHY1MEgxMHYxMGgxMHYyMGgyMFY3MGgzMFY2MEg0MFYxMGgzMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ1IiB1bmljb2RlPSJ1IiBkPSJNNzAgMEgyMHYxMEgxMHY2MGgyMFYxMGgyMHY2MGgyMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ2IiB1bmljb2RlPSJ2IiBkPSJNNzAgMjBINjBWMTBINTBWMEgzMHYxMEgyMHYxMEgxMHY1MGgyMFYyMGgyMHY1MGgyMFYyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0idyIgdW5pY29kZT0idyIgZD0iTTgwIDIwSDcwVjBINTB2MjBINDBWMEgyMHYyMEgxMHY1MGgyMFYyMGgxMHY0MGgxMFYyMGgxMHY1MGgyMFYyMHoiLz48Z2x5cGggZ2x5cGgtbmFtZT0ieCIgdW5pY29kZT0ieCIgZD0iTTcwIDBINTB2MjBIMzBWMEgxMHYyMGgxMHYxMGgxMHYxMEgyMHYxMEgxMHYyMGgyMFY1MGgyMHYyMGgyMFY1MEg2MFY0MEg1MFYzMGgxMFYyMGgxMFYweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ5IiB1bmljb2RlPSJ5IiBkPSJNNzAgMTBINjB2LTIwSDUwdi0xMEg0MHYtMTBIMHYxMGgzMHYxMGgxMFYwSDIwdjEwSDEwdjYwaDIwVjEwaDIwdjYwaDIwVjEweiIvPjxnbHlwaCBnbHlwaC1uYW1lPSJ6IiB1bmljb2RlPSJ6IiBkPSJNNzAgMEgxMHYyMGgxMHYxMGgxMHYxMGgxMHYxMGgxMHYxMEgxMHYxMGg2MFY1MEg2MFY0MEg1MFYzMEg0MFYyMEgzMFYxMGg0MFYweiIvPjwvZm9udD48L2RlZnM+PC9zdmc+) format(\"svg\");font-style:normal;font-weight:400}html{overflow:hidden;font-family:FSEX300;font-style:normal;font-stretch:normal;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:baseline;-webkit-tap-highlight-color:transparent;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;text-size-adjust:100%;background:#0e7c7c;font-size:14px}body,html{width:100%;height:100%}.banner,.content,body{display:-webkit-flex;display:flex;-webkit-align-items:center;align-items:center;-webkit-justify-content:center;justify-content:center}*{margin:0;padding:0;box-sizing:border-box}._s{border-color:#fff #757575 #757575 #fff;border-style:solid;border-width:1px;box-shadow:-1px -1px 0 0 #dadada,-1px 0 0 0 #dadada,0 -1px 0 0 #dadada,1px 1px 0 0 #000,-1px 1px 0 0 #000,1px -1px 0 0 #000}.error{width:300px;background:#c1c1c1;padding:1px}.banner{line-height:20px;height:20px;padding-left:8px;color:#fff;background:linear-gradient(left,#04056e,#157cda);background:-webkit-linear-gradient(left,#04056e,#157cda);-webkit-justify-content:space-between;justify-content:space-between}.close{margin:3px;color:grey;background:#c1c1c1;height:14px;width:16px;text-shadow:1px 1px 0 #fff;font-size:12px;line-height:1;padding:0 2px;cursor:pointer}.content{font-size:14px;padding:12px;-webkit-flex-wrap:wrap;flex-wrap:wrap}.redX{height:30px;width:30px;line-height:30px;font-size:20px;border:1px solid #000;box-shadow:2px 2px 0 0 #757575;border-radius:9rem;color:#fff;background:#ff0004}.redX,.text{text-align:center}.text{-webkit-flex-grow:1;flex-grow:1;padding:0 14px;width:242px}.btn{font-size:14px;width:75px;height:20px;margin-top:12px;text-align:center;padding:1px;cursor:pointer}.dash{border:1px dotted #000}</style></head> <body> <div class=\"error _s\"> <div class=\"banner\"> Error! <div class=\"close _s\">✕</div> </div> <div class=\"content\"> <div class=\"redX\">✕</div> <div class=\"text\"> An error has occurred while loading the page! \nOnce you are back online or the page is reachable\nagain, just press the refresh button.</div> <div class=\"btn _s\"> <div class=\"dash\">OK</div> </div> </div> </div> <script type=\"text/javascript\">!function(e){var t={};function n(r){if(t[r])return t[r].exports;var o=t[r]={i:r,l:!1,exports:{}};return e[r].call(o.exports,o,o.exports,n),o.l=!0,o.exports}n.m=e,n.c=t,n.d=function(e,t,r){n.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:r})},n.r=function(e){\"undefined\"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:\"Module\"}),Object.defineProperty(e,\"__esModule\",{value:!0})},n.t=function(e,t){if(1&t&&(e=n(e)),8&t)return e;if(4&t&&\"object\"==typeof e&&e&&e.__esModule)return e;var r=Object.create(null);if(n.r(r),Object.defineProperty(r,\"default\",{enumerable:!0,value:e}),2&t&&\"string\"!=typeof e)for(var o in e)n.d(r,o,function(t){return e[t]}.bind(null,o));return r},n.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return n.d(t,\"a\",t),t},n.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},n.p=\" \",n(n.s=0)}([function(e,t,n){e.exports=n(1)},function(e,t,n){\"use strict\";n.r(t);n(2)},function(e,t,n){}]);</script></body> </html>");
		webview.setWebViewClient(new WebViewClient() {
			
			    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (defaulterror) {
					
				}
				else {
					page_before_error = urledit.getText().toString();
					_errorpage();
				}
				
				    }
		});
		// Default error page
		FileUtil.writeFile(FileUtil.getExternalStorageDir().concat("/Browservio/error/setorerr.html"), "Successfully set error page back to default! (only this session)\n<br> <br>\nClick the back button to return to normal user session.");
		defaulterror = false;
		if (!browservio_saver.getString("defaultHomePage", "").equals("")) {
			// Load default homepage.
			webview.loadUrl(browservio_saver.getString("defaultHomePage", ""));
			urledit.setText(browservio_saver.getString("defaultHomePage", ""));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (actuallypaused) {
			_bottomBarchk();
			// Settings check
			_firstLaunch();
			crrurl = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// Make sure the page is correct
							if (webview.canGoBack()) {
								linear_control_b0.performClick();
							}
						}
					});
				}
			};
			_timer.schedule(crrurl, (int)(250));
			actuallypaused = false;
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		actuallypaused = true;
		_bottomBarchk();
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
				            Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
				                    Toast.LENGTH_LONG).show();
				        }
			    });
	}
	
	
	private void _rippleAnimator (final String _color, final View _view) {
		android.content.res.ColorStateList clr = new android.content.res.ColorStateList(new int[][]{new int[]{}},new int[]{Color.parseColor(_color)});
		android.graphics.drawable.RippleDrawable ripdr = new android.graphics.drawable.RippleDrawable(clr, null, null);
		_view.setBackground(ripdr);
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
	
	
	private void _resetduetoup () {
		dialog.setTitle("Your settings have been reset!");
		dialog.setMessage("To ensure stability, we've reset your settings to default because you've just installed an update.");
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface _dialog, int _which) {
				
			}
		});
		dialog.create().show();
	}
	
	
	private void _firstLaunch () {
		// First launch code
		if (!browservio_saver.getString("configVersion", "").equals("6") || (browservio_saver.getString("isFirstLaunch", "").equals("") || browservio_saver.getString("isFirstLaunch", "").equals("1"))) {
			browservio_saver.edit().putString("isJavaScriptEnabled", "1").commit();
			browservio_saver.edit().putString("defaultHomePage", "https://www.google.com/").commit();
			browservio_saver.edit().putString("defaultSearch", "https://www.google.com/search?q=").commit();
			browservio_saver.edit().putString("overrideEmptyError", "0").commit();
			browservio_saver.edit().putString("showBrowseBtn", "0").commit();
			if (!browservio_saver.getString("configVersion", "").equals("6") && !browservio_saver.getString("configVersion", "").equals("")) {
				_resetduetoup();
			}
			if (browservio_saver.getString("isFirstLaunch", "").equals("1")) {
				final ProgressDialog prog = new ProgressDialog(MainActivity.this); prog.setMax(100);prog.setTitle("Resetting"); prog.setMessage("Please wait..."); prog.setIndeterminate(true); prog.setCancelable(false);prog.show();
				browservio_saver.edit().putString("isFirstLaunch", "").commit();
				reset = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Intent i = getIntent();
								finish();
								startActivity(i);
								webview.clearCache(true);
								webview.clearHistory();
								reset.cancel();
								SketchwareUtil.showMessage(getApplicationContext(), "Reset successfully!");
							}
						});
					}
				};
				_timer.schedule(reset, (int)(2000));
			}
			if (!browservio_saver.getString("configVersion", "").equals("6") || browservio_saver.getString("isFirstLaunch", "").equals("")) {
				/* Load default homepage.

Current default page: https://www.google.com/ */
				webview.loadUrl("https://www.google.com/");
				urledit.setText("https://www.google.com/");
			}
			browservio_saver.edit().putString("configVersion", "6").commit();
			browservio_saver.edit().putString("isFirstLaunch", "0").commit();
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
	}
	
	
	private void _errorpage () {
		         webview.loadUrl("file:///sdcard/Browservio/error/error.html");
		//Setup media player (rewrote 200815-1307)
		errorsound = MediaPlayer.create(getApplicationContext(), R.raw.win98_error);
		errorsound.start();
		         urledit.setText("browservio://error");
	}
	
	
	private void _URLindentify (final double _type) {
		if (_type == 0) {
			if (urledit.getText().toString().equals("browservio://defaulterror") || urledit.getText().toString().equals("file:///sdcard/Browservio/error/setorerr.html")) {
				urledit.setText("browservio://defaulterror");
				webview.loadUrl("file:///sdcard/Browservio/error/setorerr.html");
				defaulterror = true;
			}
			else {
				if (urledit.getText().toString().equals("browservio://error") || urledit.getText().toString().equals("file:///sdcard/Browservio/error/error.html")) {
					_errorpage();
				}
				else {
					urledit.setText(webview.getUrl());
				}
			}
		}
		else {
			if (_type == 1) {
				if (beforepauseUrl.equals("browservio://defaulterror") || beforepauseUrl.equals("file:///sdcard/Browservio/error/setorerr.html")) {
					urledit.setText("browservio://defaulterror");
					webview.loadUrl("file:///sdcard/Browservio/error/setorerr.html");
					defaulterror = true;
				}
				else {
					if (beforepauseUrl.equals("browservio://error") || beforepauseUrl.equals("file:///sdcard/Browservio/error/error.html")) {
						_errorpage();
					}
					else {
						urledit.setText(beforepauseUrl);
					}
				}
			}
			else {
				if (_type == 2) {
					if (webview.getUrl().equals("browservio://defaulterror") || webview.getUrl().equals("file:///sdcard/Browservio/error/setorerr.html")) {
						urledit.setText("browservio://defaulterror");
						webview.loadUrl("file:///sdcard/Browservio/error/setorerr.html");
						defaulterror = true;
					}
					else {
						if (webview.getUrl().equals("browservio://error") || webview.getUrl().equals("file:///sdcard/Browservio/error/error.html")) {
							_errorpage();
						}
						else {
							urledit.setText(webview.getUrl());
						}
					}
				}
				else {
					
				}
			}
		}
	}
	
	
	private void _fullScreenVideo () {
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
	
	
	private void _bottomBarchk () {
		// Show the bottom bar how it was before pausing
		if (fabstat == 0) {
			_fab.performClick();
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
