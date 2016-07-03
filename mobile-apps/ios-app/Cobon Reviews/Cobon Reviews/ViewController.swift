//
//  ViewController.swift
//  Cobon Reviews
//
//  Created by Muhammad Hewedy on 7/3/16.
//  Copyright © 2016 Muhammad Hewedy. All rights reserved.
//

import UIKit
import SVProgressHUD

class ViewController: UIViewController, UIWebViewDelegate {

    @IBOutlet weak var webView: UIWebView!
    var backBarButton: UIBarButtonItem!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        

        // Navigation Bar setup
        self.navigationItem.title = "Cobone Reviews"
        
        self.backBarButton = UIBarButtonItem.init(title: "Back", style: UIBarButtonItemStyle.Plain, target: self, action: #selector(self.back))
        let reloadBarButton = UIBarButtonItem.init(title: "Comment", style: UIBarButtonItemStyle.Plain, target: self, action: #selector(self.addComment))
        
        backBarButton.enabled = false;
        
        self.navigationItem.leftBarButtonItem = backBarButton
        self.navigationItem.rightBarButtonItem = reloadBarButton
        
        // Web View setup
        self.webView.delegate = self
        self.webView.loadRequest(NSURLRequest.init(URL: NSURL.init(string: "https://cobone.com")!))
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // ----
    
    func back()  {
        self.webView.goBack();
    }
    
    func addComment()  {
        // TODO check the url, if a url for deal, then go to comment section, otherwise, show a message to the user
    }


    // -- UIWebViewDelegate
    
    func webViewDidStartLoad(webView: UIWebView) {
        SVProgressHUD.show()
        UIApplication.sharedApplication().networkActivityIndicatorVisible = true
    }
    
    func webViewDidFinishLoad(webView: UIWebView) {
        SVProgressHUD.dismiss()
        UIApplication.sharedApplication().networkActivityIndicatorVisible = false
        
        if (self.webView.canGoBack){
            self.backBarButton.enabled = true;
        }else{
            self.backBarButton.enabled = false;
        }
        
        if (!webView.loading){
            injectScript();
        }
    }
    
    func injectScript() {
        debugPrint("in injectScript")
        // self.webView.stringByEvaluatingJavaScriptFromString("alert('" + webView.loading.description + "')");
        
        let jsPath = NSBundle.mainBundle().pathForResource("chrome-extension/comments", ofType: "js");
        
        let commentsJs: String?
        do {
            commentsJs = try String(contentsOfFile: jsPath!, encoding: NSUTF8StringEncoding)
        } catch _ {
            commentsJs = nil
        }
        
        self.webView.stringByEvaluatingJavaScriptFromString(commentsJs!);
        
    }
}

