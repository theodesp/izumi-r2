

using System;
using System.IO;
using System.Net;
using System.Text;
using System.Collections.Specialized;
using IRT.Marshaller;
using IRT.Transport.Authorization;
using WebSocketSharp;
using IRT;

namespace IRT.Transport.Client {
    public class WebSocketTransportGeneric<C>: IClientTransport<C> where C: class, IClientTransportContext, IDisposable {
        private IJsonMarshaller marshaller;
        private WebSocket ws;
        private ILogger logger;
        private bool disposed;
        private bool connected;
        private bool connecting;

        private bool ready;
        public bool Ready {
            get {
                return ready;
            }
        }

        private string endpoint;
        public string Endpoint {
            get {
                return endpoint;
            }
            set {
                endpoint = value;
                if (!endpoint.EndsWith("\\") && !endpoint.EndsWith("/")) {
                    endpoint += "/";
                }
            }
        }

        public int Timeout; // In Seconds
        public AuthMethod Auth;

        public WebSocketTransportGeneric(string endpoint, IJsonMarshaller marshaller, ILogger logger, int timeout = 60) {
            Endpoint = endpoint;
            this.logger = logger;
            this.marshaller = marshaller;
            Timeout = timeout;
            ws = new WebSocket(endpoint);
            // ws.OnMessage += (sender, e) => {
            //     this.onMessage(e.Data);
            // }
            ws.OnOpen += (sender, e) => {
                this.connecting = false;
                this.connected = true;
            //    this.onOpen(e);
            };
            ws.OnError += (sender, e) => {
                this.logger.Logf(LogLevel.Error, "Error received");
                // this.onError(e);
            };
            ws.OnClose += (sender, e) => {
                this.connected = false;
                // this.onClose(e);
            };
            // ws.EmitOnPing = false;
            this.ready = false;
            this.connected = false;
            this.connecting = false;
        }

        public bool Open() {
            if(this.connecting || this.connected) {
                return false;
            }

            this.ws.Connect();
            return true;
        }

        public void Close() {
            if (!this.connected && !this.connecting) {
                return;
            }

            this.connected = false;
            this.connecting = false;
            this.ws.Close();
        }

        public void SetAuthorization(AuthMethod method) {
            Auth = method;
        }

        public void Send<I, O>(string service, string method, I payload, ClientTransportCallback<O> callback, C ctx) {
            try {

            }
            catch (Exception ex)
            {
                callback.Failure(
                    new TransportException("Unexpected exception occured during async request.", ex)
                );
            }
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
          if (disposed) {
              return;
          }

          if (disposing) {
             if (ws != null) {
                 ((IDisposable)ws).Dispose();
             }
          }

          disposed = true;
       }
    }
}