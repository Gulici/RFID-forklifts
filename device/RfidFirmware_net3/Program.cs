using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Configuration;
using System.Text.RegularExpressions;
using RfidFirmware;
using RfidFirmware.Configuration;
using RfidFirmware.Services;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Mocks;
using RfidFirmware.Models;
using System;
using System.Linq;

namespace RfidFirmware_net3
{
    class Program
    {
        static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureLogging(logging =>
                {
                    logging.ClearProviders();
                    logging.AddConsole();
                })
                .ConfigureServices((hostContext, services) =>
                {   
                    var env = hostContext.HostingEnvironment.EnvironmentName;
                    Console.WriteLine($"Current environment: {env}");
                    
                    ValidateReaderSettings(hostContext.Configuration);

                    services.Configure<ReaderSettings>(hostContext.Configuration.GetSection("Reader"));
                    services.Configure<ApiSettings>(hostContext.Configuration.GetSection("Api"));

                    var flags = new AppFlags
                    {
                        IsMock = args.Contains("-mock", StringComparer.OrdinalIgnoreCase),
                        IsRegister = args.Contains("-register", StringComparer.OrdinalIgnoreCase),
                        IsInteractive = args.Contains("-interactive", StringComparer.OrdinalIgnoreCase),
                        IsRegistered = CheckDeviceAlreadyRegistered()
                    };
                    services.AddSingleton(flags);

                    if (flags.IsRegister && flags.IsRegistered)
                    {
                        Console.WriteLine("Device already registered. Exiting...");
                        Environment.Exit(0);
                    }

                    if (flags.IsMock || flags.IsInteractive)
                    {
                        services.AddSingleton<IGpioService, MockGpioService>();
                        if (flags.IsInteractive)
                        {
                            services.AddSingleton<IRfidService, MockRfidServiceInteractive>();
                        }
                        else
                        {
                            services.AddSingleton<IRfidService, MockRfidService>();
                        }
                    }
                    else
                    {
                        services.AddSingleton<IGpioService, GpioService>();
                        services.AddSingleton<IRfidService, RfidServiceSpider>();
                    }

                    services.AddSingleton<IFileService, FileService>();
                    services.AddSingleton<IRegisterService, RegisterService>();

                    services.AddHttpClient<IApiService, ApiService>(client =>
                    {
                        client.BaseAddress = new Uri(hostContext.Configuration["Api:Url"]);
                        client.Timeout = TimeSpan.FromSeconds(10);
                        client.DefaultRequestHeaders.Add("Accept", "application/json");
                    });

                    services.AddSingleton<IMainService, MainService>();

                    if (flags.IsRegistered)
                    {
                        services.AddSingleton<ITagHandler, OnlineTagHandler>();
                    }
                    else
                    {
                        services.AddSingleton<ITagHandler, OfflineTagHandler>();
                    }

                    services.AddHostedService<Worker>();
                });

        private static void ValidateReaderSettings(IConfiguration configuration)
        {
            var readerSection = configuration.GetSection("Reader");
            if (!readerSection.Exists())
            {
                throw new InvalidOperationException("Section 'Reader' does not exist in appsettings.json.");
            }

            var hexRegex = new Regex("^[0-9A-Fa-f]{2}$");

            for (int i = 1; i <= 6; i++)
            {
                var key = $"TagEpc_{i}";
                var value = readerSection[key];

                if (string.IsNullOrWhiteSpace(value))
                {
                    throw new InvalidOperationException($"Missing value for {key} in appsettings.json.");
                }

                if (value.Length != 2)
                {
                    throw new InvalidOperationException($"{key} must have exactly 2 characters (current value: '{value}').");
                }

                if (!hexRegex.IsMatch(value))
                {
                    throw new InvalidOperationException($"{key} must be valid hexadecimal value (current value: '{value}').");
                }
            }
        }
                
        private static bool CheckDeviceAlreadyRegistered()
        {
            try
            {
                const string filePath = "config/device_info.json";
                if (!System.IO.File.Exists(filePath))
                    return false;

                var json = System.IO.File.ReadAllText(filePath);
                var device = System.Text.Json.JsonSerializer.Deserialize<DeviceInfo>(json);

                return device?.Registered == true;
            }
            catch
            {
                return false;
            }
        }
    }
}
